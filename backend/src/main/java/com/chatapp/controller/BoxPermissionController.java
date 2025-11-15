package com.chatapp.controller;

import com.chatapp.model.BoxPermission;
import com.chatapp.model.BoxType;
import com.chatapp.service.BoxPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Box権限管理コントローラー
 */
@RestController
@RequestMapping("/api/box-permissions")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class BoxPermissionController {

    @Autowired
    private BoxPermissionService boxPermissionService;

    /**
     * ユーザーが閲覧可能なBoxTypeのリストを取得
     * GET /api/box-permissions/accessible?organizationId=1&tenantId=1
     */
    @GetMapping("/accessible")
    public ResponseEntity<?> getAccessibleBoxTypes(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long organizationId,
            @RequestParam Long tenantId) {
        try {
            Long userId = Long.parseLong(userDetails.getUsername());
            List<BoxType> accessibleBoxTypes = boxPermissionService.getAccessibleBoxTypes(
                    userId, organizationId, tenantId
            );

            return ResponseEntity.ok(Map.of(
                    "boxTypes", accessibleBoxTypes,
                    "count", accessibleBoxTypes.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * ユーザーが投稿可能なBoxTypeのリストを取得
     * GET /api/box-permissions/postable?organizationId=1&tenantId=1
     */
    @GetMapping("/postable")
    public ResponseEntity<?> getPostableBoxTypes(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long organizationId,
            @RequestParam Long tenantId) {
        try {
            Long userId = Long.parseLong(userDetails.getUsername());
            List<BoxType> postableBoxTypes = boxPermissionService.getPostableBoxTypes(
                    userId, organizationId, tenantId
            );

            return ResponseEntity.ok(Map.of(
                    "boxTypes", postableBoxTypes,
                    "count", postableBoxTypes.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 特定のBoxTypeを閲覧可能かチェック
     * GET /api/box-permissions/can-view?organizationId=1&tenantId=1&boxTypeId=1
     */
    @GetMapping("/can-view")
    public ResponseEntity<?> canViewBoxType(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long organizationId,
            @RequestParam Long tenantId,
            @RequestParam Long boxTypeId) {
        try {
            Long userId = Long.parseLong(userDetails.getUsername());
            boolean canView = boxPermissionService.canViewBoxType(
                    userId, organizationId, tenantId, boxTypeId
            );

            return ResponseEntity.ok(Map.of(
                    "canView", canView,
                    "boxTypeId", boxTypeId
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 特定のBoxTypeに投稿可能かチェック
     * GET /api/box-permissions/can-post?organizationId=1&tenantId=1&boxTypeId=1
     */
    @GetMapping("/can-post")
    public ResponseEntity<?> canPostToBoxType(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long organizationId,
            @RequestParam Long tenantId,
            @RequestParam Long boxTypeId) {
        try {
            Long userId = Long.parseLong(userDetails.getUsername());
            boolean canPost = boxPermissionService.canPostToBoxType(
                    userId, organizationId, tenantId, boxTypeId
            );

            return ResponseEntity.ok(Map.of(
                    "canPost", canPost,
                    "boxTypeId", boxTypeId
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * ユーザーのロールレベルを取得
     * GET /api/box-permissions/role-level?organizationId=1
     */
    @GetMapping("/role-level")
    public ResponseEntity<?> getUserRoleLevel(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long organizationId) {
        try {
            Long userId = Long.parseLong(userDetails.getUsername());
            Integer roleLevel = boxPermissionService.getUserRoleLevel(userId, organizationId);

            return ResponseEntity.ok(Map.of(
                    "roleLevel", roleLevel,
                    "userId", userId
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * テナント内の全BoxTypeを取得
     * GET /api/box-permissions/all-box-types?tenantId=1
     */
    @GetMapping("/all-box-types")
    public ResponseEntity<?> getAllBoxTypes(@RequestParam Long tenantId) {
        try {
            List<BoxType> boxTypes = boxPermissionService.getAllBoxTypes(tenantId);

            return ResponseEntity.ok(Map.of(
                    "boxTypes", boxTypes,
                    "count", boxTypes.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 権限を付与
     * POST /api/box-permissions/grant
     */
    @PostMapping("/grant")
    public ResponseEntity<?> grantPermission(
            @RequestBody GrantPermissionRequest request) {
        try {
            BoxPermission permission = boxPermissionService.grantPermission(
                    request.getUserId(),
                    request.getBoxTypeId(),
                    request.getOrganizationId(),
                    request.getTenantId(),
                    request.isCanView(),
                    request.isCanPost(),
                    request.isCanReply(),
                    request.isCanModerate()
            );

            return ResponseEntity.ok(Map.of(
                    "message", "権限を付与しました",
                    "permission", permission
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * ユーザーの特定BoxTypeに対する権限を取得
     * GET /api/box-permissions/user-permission?userId=1&boxTypeId=1&organizationId=1
     */
    @GetMapping("/user-permission")
    public ResponseEntity<?> getUserBoxPermission(
            @RequestParam Long userId,
            @RequestParam Long boxTypeId,
            @RequestParam Long organizationId) {
        try {
            Optional<BoxPermission> permission = boxPermissionService.getUserBoxPermission(
                    userId, boxTypeId, organizationId
            );

            if (permission.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "hasPermission", false,
                        "message", "権限が見つかりません"
                ));
            }

            return ResponseEntity.ok(Map.of(
                    "hasPermission", true,
                    "permission", permission.get()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // リクエストDTO
    public static class GrantPermissionRequest {
        private Long userId;
        private Long boxTypeId;
        private Long organizationId;
        private Long tenantId;
        private boolean canView;
        private boolean canPost;
        private boolean canReply;
        private boolean canModerate;

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public Long getBoxTypeId() {
            return boxTypeId;
        }

        public void setBoxTypeId(Long boxTypeId) {
            this.boxTypeId = boxTypeId;
        }

        public Long getOrganizationId() {
            return organizationId;
        }

        public void setOrganizationId(Long organizationId) {
            this.organizationId = organizationId;
        }

        public Long getTenantId() {
            return tenantId;
        }

        public void setTenantId(Long tenantId) {
            this.tenantId = tenantId;
        }

        public boolean isCanView() {
            return canView;
        }

        public void setCanView(boolean canView) {
            this.canView = canView;
        }

        public boolean isCanPost() {
            return canPost;
        }

        public void setCanPost(boolean canPost) {
            this.canPost = canPost;
        }

        public boolean isCanReply() {
            return canReply;
        }

        public void setCanReply(boolean canReply) {
            this.canReply = canReply;
        }

        public boolean isCanModerate() {
            return canModerate;
        }

        public void setCanModerate(boolean canModerate) {
            this.canModerate = canModerate;
        }
    }
}
