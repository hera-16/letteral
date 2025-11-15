package com.chatapp.controller;

import com.chatapp.annotation.RequireRole;
import com.chatapp.model.RoleName;
import com.chatapp.model.RolePromotionRequest;
import com.chatapp.service.RolePromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * ロール昇格申請コントローラー
 */
@RestController
@RequestMapping("/api/promotions")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class RolePromotionController {

    @Autowired
    private RolePromotionService promotionService;

    /**
     * 昇格申請を作成
     * POST /api/promotions
     */
    @PostMapping
    public ResponseEntity<?> createPromotionRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CreatePromotionRequest request) {
        try {
            Long userId = Long.parseLong(userDetails.getUsername());

            RolePromotionRequest promotion = promotionService.createPromotionRequest(
                    userId,
                    request.getOrganizationId(),
                    request.getCurrentRole(),
                    request.getRequestedRole(),
                    request.getReason()
            );

            return ResponseEntity.ok(Map.of(
                    "message", "昇格申請を提出しました",
                    "promotion", promotion
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "昇格申請の作成に失敗しました"));
        }
    }

    /**
     * 昇格申請を承認
     * POST /api/promotions/{requestId}/approve
     */
    @PostMapping("/{requestId}/approve")
    @RequireRole(RoleName.MANAGER)
    public ResponseEntity<?> approvePromotion(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long requestId,
            @RequestBody ReviewRequest request) {
        try {
            Long approvedByUserId = Long.parseLong(userDetails.getUsername());

            promotionService.approvePromotion(requestId, approvedByUserId, request.getComment());

            return ResponseEntity.ok(Map.of("message", "昇格申請を承認しました"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 昇格申請を却下
     * POST /api/promotions/{requestId}/reject
     */
    @PostMapping("/{requestId}/reject")
    @RequireRole(RoleName.MANAGER)
    public ResponseEntity<?> rejectPromotion(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long requestId,
            @RequestBody ReviewRequest request) {
        try {
            Long rejectedByUserId = Long.parseLong(userDetails.getUsername());

            promotionService.rejectPromotion(requestId, rejectedByUserId, request.getComment());

            return ResponseEntity.ok(Map.of("message", "昇格申請を却下しました"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 組織内の承認待ち昇格申請一覧を取得
     * GET /api/promotions/pending?organizationId=1
     */
    @GetMapping("/pending")
    @RequireRole(RoleName.PM)
    public ResponseEntity<?> getPendingRequests(@RequestParam Long organizationId) {
        try {
            List<RolePromotionRequest> requests = promotionService.getPendingRequests(organizationId);
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 自分の昇格申請履歴を取得
     * GET /api/promotions/my-history
     */
    @GetMapping("/my-history")
    public ResponseEntity<?> getMyPromotionHistory(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long userId = Long.parseLong(userDetails.getUsername());
            List<RolePromotionRequest> history = promotionService.getUserPromotionHistory(userId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 昇格申請の詳細を取得
     * GET /api/promotions/{requestId}
     */
    @GetMapping("/{requestId}")
    public ResponseEntity<?> getPromotionRequest(@PathVariable Long requestId) {
        try {
            Optional<RolePromotionRequest> request = promotionService.getPromotionRequest(requestId);

            if (request.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(request.get());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 昇格条件を満たしているかチェック
     * GET /api/promotions/check-eligibility?userId=1&organizationId=1&tenantId=1&targetRole=MANAGER
     */
    @GetMapping("/check-eligibility")
    public ResponseEntity<?> checkPromotionEligibility(
            @RequestParam Long userId,
            @RequestParam Long organizationId,
            @RequestParam Long tenantId,
            @RequestParam RoleName targetRole) {
        try {
            boolean meetsRequirements = promotionService.meetsPromotionRequirements(
                    userId, organizationId, tenantId, targetRole
            );

            return ResponseEntity.ok(Map.of(
                    "meetsRequirements", meetsRequirements,
                    "targetRole", targetRole
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 昇格可能なロール一覧を取得（条件を満たしているもののみ）
     * GET /api/promotions/eligible-roles?userId=1&organizationId=1&tenantId=1
     */
    @GetMapping("/eligible-roles")
    public ResponseEntity<?> getEligiblePromotions(
            @RequestParam Long userId,
            @RequestParam Long organizationId,
            @RequestParam Long tenantId) {
        try {
            List<RoleName> eligibleRoles = promotionService.getEligiblePromotions(
                    userId, organizationId, tenantId
            );

            return ResponseEntity.ok(Map.of(
                    "eligibleRoles", eligibleRoles
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // リクエストDTO
    public static class CreatePromotionRequest {
        private Long organizationId;
        private RoleName currentRole;
        private RoleName requestedRole;
        private String reason;

        public Long getOrganizationId() {
            return organizationId;
        }

        public void setOrganizationId(Long organizationId) {
            this.organizationId = organizationId;
        }

        public RoleName getCurrentRole() {
            return currentRole;
        }

        public void setCurrentRole(RoleName currentRole) {
            this.currentRole = currentRole;
        }

        public RoleName getRequestedRole() {
            return requestedRole;
        }

        public void setRequestedRole(RoleName requestedRole) {
            this.requestedRole = requestedRole;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }

    public static class ReviewRequest {
        private String comment;

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }
    }
}
