package com.chatapp.controller;

import com.chatapp.annotation.RequireRole;
import com.chatapp.model.*;
import com.chatapp.service.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ユーザーロール管理コントローラー
 */
@RestController
@RequestMapping("/api/roles")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class UserRoleController {

    @Autowired
    private UserRoleService userRoleService;

    /**
     * 現在のユーザーのロール情報を取得
     * GET /api/roles/me?organizationId=1
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUserRole(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long organizationId) {
        try {
            Long userId = Long.parseLong(userDetails.getUsername());
            Optional<UserRole> role = userRoleService.getCurrentRole(userId, organizationId);

            if (role.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "message", "ロールが割り当てられていません",
                        "hasRole", false
                ));
            }

            return ResponseEntity.ok(Map.of(
                    "hasRole", true,
                    "role", role.get()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * テナント内の全ロール統計を取得
     * GET /api/roles/statistics?tenantId=1
     */
    @GetMapping("/statistics")
    @RequireRole(RoleName.MANAGER)
    public ResponseEntity<?> getRoleStatistics(@RequestParam Long tenantId) {
        try {
            Map<RoleName, Long> statistics = userRoleService.getRoleStatistics(tenantId);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 組織内のユーザーロール一覧を取得
     * GET /api/roles/organization/{organizationId}
     */
    @GetMapping("/organization/{organizationId}")
    @RequireRole(RoleName.PM)
    public ResponseEntity<?> getOrganizationRoles(@PathVariable Long organizationId) {
        try {
            List<UserRole> roles = userRoleService.getOrganizationRoles(organizationId);
            return ResponseEntity.ok(roles);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * ユーザーにロールを割り当て
     * POST /api/roles/assign
     */
    @PostMapping("/assign")
    public ResponseEntity<?> assignRole(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody AssignRoleRequest request) {
        try {
            Long assignedByUserId = Long.parseLong(userDetails.getUsername());

            UserRole newRole = userRoleService.assignRole(
                    request.getUserId(),
                    request.getRoleName(),
                    request.getTenantId(),
                    request.getOrganizationId(),
                    assignedByUserId
            );

            return ResponseEntity.ok(Map.of(
                    "message", "ロールを割り当てました",
                    "role", newRole
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * ロールを取り消し
     * DELETE /api/roles/{roleId}
     */
    @DeleteMapping("/{roleId}")
    public ResponseEntity<?> revokeRole(@PathVariable Long roleId) {
        try {
            userRoleService.revokeRole(roleId);
            return ResponseEntity.ok(Map.of("message", "ロールを取り消しました"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 昇格可能なロールのリストを取得
     * GET /api/roles/promotable?currentRole=MEMBER&tenantId=1
     */
    @GetMapping("/promotable")
    public ResponseEntity<?> getPromotableRoles(
            @RequestParam RoleName currentRole,
            @RequestParam Long tenantId) {
        try {
            List<RoleName> promotableRoles = userRoleService.getPromotableRoles(currentRole, tenantId);
            return ResponseEntity.ok(promotableRoles);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * ユーザーが特定のロール以上の権限を持っているかチェック
     * GET /api/roles/check?userId=1&organizationId=1&minimumRole=MANAGER
     */
    @GetMapping("/check")
    public ResponseEntity<?> checkRolePermission(
            @RequestParam Long userId,
            @RequestParam Long organizationId,
            @RequestParam RoleName minimumRole) {
        try {
            boolean hasPermission = userRoleService.hasRoleOrHigher(userId, organizationId, minimumRole);
            return ResponseEntity.ok(Map.of(
                    "hasPermission", hasPermission,
                    "minimumRole", minimumRole
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // リクエストDTO
    public static class AssignRoleRequest {
        private Long userId;
        private RoleName roleName;
        private Long tenantId;
        private Long organizationId;

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public RoleName getRoleName() {
            return roleName;
        }

        public void setRoleName(RoleName roleName) {
            this.roleName = roleName;
        }

        public Long getTenantId() {
            return tenantId;
        }

        public void setTenantId(Long tenantId) {
            this.tenantId = tenantId;
        }

        public Long getOrganizationId() {
            return organizationId;
        }

        public void setOrganizationId(Long organizationId) {
            this.organizationId = organizationId;
        }
    }
}
