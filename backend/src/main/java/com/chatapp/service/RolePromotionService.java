package com.chatapp.service;

import com.chatapp.model.*;
import com.chatapp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * ロール昇格申請サービス
 */
@Service
public class RolePromotionService {

    @Autowired
    private RolePromotionRequestRepository promotionRequestRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private RoleHierarchyRepository roleHierarchyRepository;

    @Autowired
    private UserRoleService userRoleService;

    /**
     * 昇格申請を作成
     */
    @Transactional
    public RolePromotionRequest createPromotionRequest(Long userId, Long organizationId,
                                                       RoleName currentRole, RoleName requestedRole,
                                                       String reason) {
        // 既存の承認待ち申請があるかチェック
        Optional<RolePromotionRequest> existingRequest = promotionRequestRepository
                .findAll().stream()
                .filter(req -> req.getUserId().equals(userId))
                .filter(req -> req.getOrganizationId().equals(organizationId))
                .filter(req -> "PENDING".equals(req.getStatus()))
                .findFirst();

        if (existingRequest.isPresent()) {
            throw new IllegalStateException("既に承認待ちの昇格申請が存在します");
        }

        // 昇格申請を作成
        RolePromotionRequest request = new RolePromotionRequest();
        request.setUserId(userId);
        request.setOrganizationId(organizationId);
        request.setCurrentRoleLevel(currentRole.getLevel());
        request.setRequestedRoleLevel(requestedRole.getLevel());
        request.setRequestedRoleName(requestedRole.name());
        request.setReason(reason);
        request.setStatus("PENDING");
        request.setCreatedAt(LocalDateTime.now());

        return promotionRequestRepository.save(request);
    }

    /**
     * 昇格申請を承認
     */
    @Transactional
    public void approvePromotion(Long requestId, Long approvedByUserId, String comment) {
        RolePromotionRequest request = promotionRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("昇格申請が見つかりません"));

        if (!"PENDING".equals(request.getStatus())) {
            throw new IllegalStateException("この申請は既に処理済みです");
        }

        // ユーザーのロールを更新
        Optional<UserRole> userRole = userRoleService.getCurrentRole(
                request.getUserId(),
                request.getOrganizationId()
        );

        if (userRole.isPresent()) {
            RoleName requestedRole = RoleName.fromString(request.getRequestedRoleName());
            UserRole role = userRole.get();
            role.setRoleName(requestedRole);
            role.setRoleLevel(requestedRole.getLevel());
            role.setApprovedBy(approvedByUserId);
            role.setApprovedAt(LocalDateTime.now());
            userRoleRepository.save(role);
        }

        // 申請のステータスを更新
        request.setStatus("APPROVED");
        request.setReviewedBy(approvedByUserId);
        request.setReviewedAt(LocalDateTime.now());
        request.setReviewComment(comment);

        promotionRequestRepository.save(request);
    }

    /**
     * 昇格申請を却下
     */
    @Transactional
    public void rejectPromotion(Long requestId, Long rejectedByUserId, String comment) {
        RolePromotionRequest request = promotionRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("昇格申請が見つかりません"));

        if (!"PENDING".equals(request.getStatus())) {
            throw new IllegalStateException("この申請は既に処理済みです");
        }

        request.setStatus("REJECTED");
        request.setReviewedBy(rejectedByUserId);
        request.setReviewedAt(LocalDateTime.now());
        request.setReviewComment(comment);

        promotionRequestRepository.save(request);
    }

    /**
     * 組織内の承認待ち昇格申請一覧を取得
     */
    public List<RolePromotionRequest> getPendingRequests(Long organizationId) {
        return promotionRequestRepository.findAll().stream()
                .filter(req -> req.getOrganizationId().equals(organizationId))
                .filter(req -> "PENDING".equals(req.getStatus()))
                .toList();
    }

    /**
     * ユーザーの昇格申請履歴を取得
     */
    public List<RolePromotionRequest> getUserPromotionHistory(Long userId) {
        return promotionRequestRepository.findAll().stream()
                .filter(req -> req.getUserId().equals(userId))
                .toList();
    }

    /**
     * 昇格申請の詳細を取得
     */
    public Optional<RolePromotionRequest> getPromotionRequest(Long requestId) {
        return promotionRequestRepository.findById(requestId);
    }

    /**
     * ユーザーが昇格条件を満たしているかチェック
     */
    public boolean meetsPromotionRequirements(Long userId, Long organizationId,
                                               Long tenantId, RoleName targetRole) {
        // 現在のロールを取得
        Optional<UserRole> currentRole = userRoleService.getCurrentRole(userId, organizationId);
        if (currentRole.isEmpty()) {
            return false;
        }

        UserRole userRole = currentRole.get();

        // ターゲットロールの条件を取得
        Optional<RoleHierarchy> targetHierarchy = roleHierarchyRepository.findAll().stream()
                .filter(h -> h.getTenantId().equals(tenantId))
                .filter(h -> h.getRoleName().equals(targetRole))
                .findFirst();

        if (targetHierarchy.isEmpty()) {
            return false;
        }

        RoleHierarchy hierarchy = targetHierarchy.get();

        // 条件をチェック
        return userRole.meetsPromotionRequirements(
                hierarchy.getMinPostsRequired(),
                hierarchy.getMinDaysActive()
        );
    }

    /**
     * 昇格可能なロール一覧を取得（条件を満たしているもののみ）
     */
    public List<RoleName> getEligiblePromotions(Long userId, Long organizationId, Long tenantId) {
        Optional<UserRole> currentRole = userRoleService.getCurrentRole(userId, organizationId);
        if (currentRole.isEmpty()) {
            return List.of();
        }

        RoleName current = currentRole.get().getRoleName();
        List<RoleName> promotableRoles = userRoleService.getPromotableRoles(current, tenantId);

        // 条件を満たしているロールのみフィルタリング
        return promotableRoles.stream()
                .filter(role -> meetsPromotionRequirements(userId, organizationId, tenantId, role))
                .toList();
    }
}
