package com.chatapp.controller;

import com.chatapp.model.AdminActionLog;
import com.chatapp.model.User;
import com.chatapp.service.AdminActionLogService;
import com.chatapp.service.AdminDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminDashboardController {

    @Autowired
    private AdminDashboardService adminDashboardService;

    @Autowired
    private AdminActionLogService adminActionLogService;

    /**
     * ダッシュボード統計を取得
     */
    @GetMapping("/dashboard/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics(@AuthenticationPrincipal User user) {
        Map<String, Object> stats = adminDashboardService.getDashboardStatistics(user.getTenantId());
        return ResponseEntity.ok(stats);
    }

    /**
     * ユーザー増加データを取得
     */
    @GetMapping("/dashboard/user-growth")
    public ResponseEntity<Map<String, Object>> getUserGrowth(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "30") int days) {
        Map<String, Object> data = adminDashboardService.getUserGrowthData(user.getTenantId(), days);
        return ResponseEntity.ok(data);
    }

    /**
     * アクティビティトレンドを取得
     */
    @GetMapping("/dashboard/activity-trends")
    public ResponseEntity<Map<String, Object>> getActivityTrends(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "7") int days) {
        Map<String, Object> trends = adminDashboardService.getActivityTrends(user.getTenantId(), days);
        return ResponseEntity.ok(trends);
    }

    /**
     * トップコントリビューターを取得
     */
    @GetMapping("/dashboard/top-contributors")
    public ResponseEntity<Map<String, Object>> getTopContributors(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "10") int limit) {
        Map<String, Object> contributors = adminDashboardService.getTopContributors(user.getTenantId(), limit);
        return ResponseEntity.ok(contributors);
    }

    /**
     * 管理者アクションログを取得
     */
    @GetMapping("/action-logs")
    public ResponseEntity<Page<AdminActionLog>> getActionLogs(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AdminActionLog> logs = adminActionLogService.getLogsByTenant(user.getTenantId(), pageable);
        return ResponseEntity.ok(logs);
    }

    /**
     * 特定の管理者のアクションログを取得
     */
    @GetMapping("/action-logs/admin/{adminId}")
    public ResponseEntity<List<AdminActionLog>> getLogsByAdmin(
            @AuthenticationPrincipal User user,
            @PathVariable Long adminId) {
        List<AdminActionLog> logs = adminActionLogService.getLogsByAdmin(user.getTenantId(), adminId);
        return ResponseEntity.ok(logs);
    }

    /**
     * 最近のアクションログを取得
     */
    @GetMapping("/action-logs/recent")
    public ResponseEntity<List<AdminActionLog>> getRecentLogs(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "7") int days) {
        List<AdminActionLog> logs = adminActionLogService.getRecentLogs(user.getTenantId(), days);
        return ResponseEntity.ok(logs);
    }

    /**
     * 管理者アクションを記録
     */
    @PostMapping("/action-logs")
    public ResponseEntity<AdminActionLog> logAction(
            @AuthenticationPrincipal User user,
            @RequestBody LogActionRequest request) {
        AdminActionLog log = adminActionLogService.logAction(
                user.getTenantId(),
                user.getId(),
                request.getActionType(),
                request.getTargetType(),
                request.getTargetId(),
                request.getDescription(),
                request.getMetadata()
        );
        return ResponseEntity.ok(log);
    }

    // DTOクラス
    public static class LogActionRequest {
        private String actionType;
        private String targetType;
        private Long targetId;
        private String description;
        private String metadata;

        public String getActionType() { return actionType; }
        public void setActionType(String actionType) { this.actionType = actionType; }
        public String getTargetType() { return targetType; }
        public void setTargetType(String targetType) { this.targetType = targetType; }
        public Long getTargetId() { return targetId; }
        public void setTargetId(Long targetId) { this.targetId = targetId; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getMetadata() { return metadata; }
        public void setMetadata(String metadata) { this.metadata = metadata; }
    }
}
