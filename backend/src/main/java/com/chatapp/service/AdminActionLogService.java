package com.chatapp.service;

import com.chatapp.model.AdminActionLog;
import com.chatapp.model.User;
import com.chatapp.repository.AdminActionLogRepository;
import com.chatapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminActionLogService {

    private final AdminActionLogRepository adminActionLogRepository;
    private final UserRepository userRepository;

    @Transactional
    public AdminActionLog logAction(Long tenantId, Long adminUserId, String actionType,
                                    String targetType, Long targetId, String description, String metadata) {
        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new RuntimeException("Admin user not found"));

        AdminActionLog log = new AdminActionLog();
        log.setTenantId(tenantId);
        log.setAdminUser(admin);
        log.setActionType(actionType);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDescription(description);
        log.setMetadata(metadata);

        return adminActionLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public Page<AdminActionLog> getLogsByTenant(Long tenantId, Pageable pageable) {
        return adminActionLogRepository.findByTenantIdOrderByCreatedAtDesc(tenantId, pageable);
    }

    @Transactional(readOnly = true)
    public List<AdminActionLog> getLogsByAdmin(Long tenantId, Long adminUserId) {
        return adminActionLogRepository.findByTenantIdAndAdminUserIdOrderByCreatedAtDesc(tenantId, adminUserId);
    }

    @Transactional(readOnly = true)
    public List<AdminActionLog> getRecentLogs(Long tenantId, int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        return adminActionLogRepository.findRecentLogs(tenantId, startDate);
    }
}
