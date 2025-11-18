package com.chatapp.service;

import com.chatapp.model.SlackIntegration;
import com.chatapp.repository.SlackIntegrationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SlackIntegrationService {

    private final SlackIntegrationRepository slackIntegrationRepository;
    private final AdminActionLogService adminActionLogService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public SlackIntegration createIntegration(Long tenantId, String workspaceId, String botToken,
                                              String webhookUrl, String defaultChannel, Long adminUserId) {
        SlackIntegration integration = new SlackIntegration();
        integration.setTenantId(tenantId);
        integration.setWorkspaceId(workspaceId);
        integration.setBotToken(botToken);
        integration.setWebhookUrl(webhookUrl);
        integration.setDefaultChannel(defaultChannel);
        integration.setIsActive(true);

        SlackIntegration saved = slackIntegrationRepository.save(integration);

        // ログ記録
        adminActionLogService.logAction(
                tenantId, adminUserId, "CREATE_SLACK_INTEGRATION",
                "SlackIntegration", saved.getId(),
                "Slack integration created for workspace: " + workspaceId,
                null
        );

        return saved;
    }

    @Transactional(readOnly = true)
    public List<SlackIntegration> getIntegrationsByTenant(Long tenantId) {
        return slackIntegrationRepository.findByTenantId(tenantId);
    }

    @Transactional(readOnly = true)
    public Optional<SlackIntegration> getActiveIntegration(Long tenantId) {
        return slackIntegrationRepository.findByTenantIdAndIsActive(tenantId, true);
    }

    @Transactional
    public SlackIntegration updateIntegration(Long integrationId, String botToken, String webhookUrl,
                                              String defaultChannel, Boolean isActive, Long adminUserId) {
        SlackIntegration integration = slackIntegrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("Slack integration not found"));

        if (botToken != null) integration.setBotToken(botToken);
        if (webhookUrl != null) integration.setWebhookUrl(webhookUrl);
        if (defaultChannel != null) integration.setDefaultChannel(defaultChannel);
        if (isActive != null) integration.setIsActive(isActive);

        SlackIntegration updated = slackIntegrationRepository.save(integration);

        // ログ記録
        adminActionLogService.logAction(
                integration.getTenantId(), adminUserId, "UPDATE_SLACK_INTEGRATION",
                "SlackIntegration", integrationId,
                "Slack integration updated",
                null
        );

        return updated;
    }

    @Transactional
    public void deleteIntegration(Long integrationId, Long adminUserId) {
        SlackIntegration integration = slackIntegrationRepository.findById(integrationId)
                .orElseThrow(() -> new RuntimeException("Slack integration not found"));

        Long tenantId = integration.getTenantId();
        slackIntegrationRepository.delete(integration);

        // ログ記録
        adminActionLogService.logAction(
                tenantId, adminUserId, "DELETE_SLACK_INTEGRATION",
                "SlackIntegration", integrationId,
                "Slack integration deleted",
                null
        );
    }

    /**
     * Slackにメッセージを送信
     */
    public void sendMessage(Long tenantId, String channel, String text, Map<String, Object> blocks) {
        Optional<SlackIntegration> integrationOpt = getActiveIntegration(tenantId);
        if (integrationOpt.isEmpty()) {
            throw new RuntimeException("No active Slack integration found for tenant");
        }

        SlackIntegration integration = integrationOpt.get();

        try {
            if (integration.getWebhookUrl() != null && !integration.getWebhookUrl().isEmpty()) {
                sendViaWebhook(integration.getWebhookUrl(), text, blocks);
            } else {
                sendViaBotToken(integration.getBotToken(), channel != null ? channel : integration.getDefaultChannel(), text, blocks);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to send Slack message: " + e.getMessage(), e);
        }
    }

    private void sendViaWebhook(String webhookUrl, String text, Map<String, Object> blocks) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("text", text);
        if (blocks != null) {
            payload.put("blocks", blocks);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(payload), headers);
        restTemplate.postForEntity(webhookUrl, request, String.class);
    }

    private void sendViaBotToken(String botToken, String channel, String text, Map<String, Object> blocks) throws Exception {
        String url = "https://slack.com/api/chat.postMessage";

        Map<String, Object> payload = new HashMap<>();
        payload.put("channel", channel);
        payload.put("text", text);
        if (blocks != null) {
            payload.put("blocks", blocks);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(botToken);

        HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(payload), headers);
        restTemplate.postForEntity(url, request, String.class);
    }

    /**
     * 進捗投稿の通知を送信
     */
    public void notifyProgressPost(Long tenantId, String userName, String content, String channel) {
        String text = String.format("新しい進捗投稿: %s\n%s", userName, content);
        sendMessage(tenantId, channel, text, null);
    }

    /**
     * 通報の通知を送信
     */
    public void notifyReport(Long tenantId, String reportType, String reason, String channel) {
        String text = String.format("新しい通報がありました\nタイプ: %s\n理由: %s", reportType, reason);
        sendMessage(tenantId, channel, text, null);
    }

    /**
     * OKR達成の通知を送信
     */
    public void notifyOKRCompletion(Long tenantId, String okrTitle, String userName, String channel) {
        String text = String.format("OKR達成: %s\n達成者: %s", okrTitle, userName);
        sendMessage(tenantId, channel, text, null);
    }
}
