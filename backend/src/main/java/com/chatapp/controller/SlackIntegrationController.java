package com.chatapp.controller;

import com.chatapp.model.SlackIntegration;
import com.chatapp.model.User;
import com.chatapp.service.SlackIntegrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/slack")
public class SlackIntegrationController {

    @Autowired
    private SlackIntegrationService slackIntegrationService;

    /**
     * Slack連携を作成
     */
    @PostMapping("/integrations")
    public ResponseEntity<SlackIntegration> createIntegration(
            @AuthenticationPrincipal User user,
            @RequestBody CreateIntegrationRequest request) {
        SlackIntegration integration = slackIntegrationService.createIntegration(
                user.getTenantId(),
                request.getWorkspaceId(),
                request.getBotToken(),
                request.getWebhookUrl(),
                request.getDefaultChannel(),
                user.getId()
        );
        return ResponseEntity.ok(integration);
    }

    /**
     * テナントのSlack連携一覧を取得
     */
    @GetMapping("/integrations")
    public ResponseEntity<List<SlackIntegration>> getIntegrations(@AuthenticationPrincipal User user) {
        List<SlackIntegration> integrations = slackIntegrationService.getIntegrationsByTenant(user.getTenantId());
        return ResponseEntity.ok(integrations);
    }

    /**
     * アクティブなSlack連携を取得
     */
    @GetMapping("/integrations/active")
    public ResponseEntity<SlackIntegration> getActiveIntegration(@AuthenticationPrincipal User user) {
        return slackIntegrationService.getActiveIntegration(user.getTenantId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Slack連携を更新
     */
    @PutMapping("/integrations/{id}")
    public ResponseEntity<SlackIntegration> updateIntegration(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @RequestBody UpdateIntegrationRequest request) {
        SlackIntegration integration = slackIntegrationService.updateIntegration(
                id,
                request.getBotToken(),
                request.getWebhookUrl(),
                request.getDefaultChannel(),
                request.getIsActive(),
                user.getId()
        );
        return ResponseEntity.ok(integration);
    }

    /**
     * Slack連携を削除
     */
    @DeleteMapping("/integrations/{id}")
    public ResponseEntity<Void> deleteIntegration(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        slackIntegrationService.deleteIntegration(id, user.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * テストメッセージを送信
     */
    @PostMapping("/test")
    public ResponseEntity<Map<String, String>> sendTestMessage(
            @AuthenticationPrincipal User user,
            @RequestBody TestMessageRequest request) {
        try {
            slackIntegrationService.sendMessage(
                    user.getTenantId(),
                    request.getChannel(),
                    request.getMessage(),
                    null
            );
            return ResponseEntity.ok(Map.of("status", "success", "message", "Test message sent successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    // DTOクラス
    public static class CreateIntegrationRequest {
        private String workspaceId;
        private String botToken;
        private String webhookUrl;
        private String defaultChannel;

        public String getWorkspaceId() { return workspaceId; }
        public void setWorkspaceId(String workspaceId) { this.workspaceId = workspaceId; }
        public String getBotToken() { return botToken; }
        public void setBotToken(String botToken) { this.botToken = botToken; }
        public String getWebhookUrl() { return webhookUrl; }
        public void setWebhookUrl(String webhookUrl) { this.webhookUrl = webhookUrl; }
        public String getDefaultChannel() { return defaultChannel; }
        public void setDefaultChannel(String defaultChannel) { this.defaultChannel = defaultChannel; }
    }

    public static class UpdateIntegrationRequest {
        private String botToken;
        private String webhookUrl;
        private String defaultChannel;
        private Boolean isActive;

        public String getBotToken() { return botToken; }
        public void setBotToken(String botToken) { this.botToken = botToken; }
        public String getWebhookUrl() { return webhookUrl; }
        public void setWebhookUrl(String webhookUrl) { this.webhookUrl = webhookUrl; }
        public String getDefaultChannel() { return defaultChannel; }
        public void setDefaultChannel(String defaultChannel) { this.defaultChannel = defaultChannel; }
        public Boolean getIsActive() { return isActive; }
        public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    }

    public static class TestMessageRequest {
        private String channel;
        private String message;

        public String getChannel() { return channel; }
        public void setChannel(String channel) { this.channel = channel; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
