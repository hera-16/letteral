package com.chatapp.service;

import com.chatapp.model.*;
import com.chatapp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Box権限管理サービス
 * ユーザーのロールに基づいて、Boxの閲覧権限を制御
 */
@Service
public class BoxPermissionService {

    @Autowired
    private BoxPermissionRepository boxPermissionRepository;

    @Autowired
    private BoxTypeRepository boxTypeRepository;

    @Autowired
    private UserRoleService userRoleService;

    // Note: This repository is kept for future use when implementing role hierarchy checks
    @SuppressWarnings("unused")
    @Autowired
    private RoleHierarchyRepository roleHierarchyRepository;

    /**
     * ユーザーが特定のBoxTypeを閲覧可能かチェック
     */
    public boolean canViewBoxType(Long userId, Long organizationId, Long tenantId, Long boxTypeId) {
        // BoxType情報を取得
        Optional<BoxType> boxTypeOptional = boxTypeRepository.findById(boxTypeId);
        if (boxTypeOptional.isEmpty()) {
            return false;
        }

        BoxType boxType = boxTypeOptional.get();

        // ユーザーのロールを取得
        Optional<UserRole> userRole = userRoleService.getCurrentRole(userId, organizationId);
        if (userRole.isEmpty()) {
            return false;
        }

        Integer roleLevel = userRole.get().getRoleLevel();

        // BoxTypeの最小閲覧レベルと比較
        return roleLevel >= boxType.getMinRoleLevelToView();
    }

    /**
     * ユーザーが特定のBoxTypeに投稿可能かチェック
     */
    public boolean canPostToBoxType(Long userId, Long organizationId, Long tenantId, Long boxTypeId) {
        // BoxType情報を取得
        Optional<BoxType> boxTypeOptional = boxTypeRepository.findById(boxTypeId);
        if (boxTypeOptional.isEmpty()) {
            return false;
        }

        BoxType boxType = boxTypeOptional.get();

        // ユーザーのロールを取得
        Optional<UserRole> userRole = userRoleService.getCurrentRole(userId, organizationId);
        if (userRole.isEmpty()) {
            return false;
        }

        Integer roleLevel = userRole.get().getRoleLevel();

        // BoxTypeの最小投稿レベルと比較
        return roleLevel >= boxType.getMinRoleLevelToPost();
    }

    /**
     * ユーザーが特定のBoxTypeに返信可能かチェック
     */
    public boolean canReplyToBoxType(Long userId, Long organizationId, Long tenantId, Long boxTypeId) {
        // BoxPermissionレコードをチェック
        Optional<BoxPermission> permission = boxPermissionRepository
                .findByUserIdAndBoxTypeIdAndOrganizationId(userId, boxTypeId, organizationId);

        if (permission.isPresent() && permission.get().isValid()) {
            return permission.get().getCanReply();
        }

        // BoxPermissionが無い場合は投稿権限で判定
        return canPostToBoxType(userId, organizationId, tenantId, boxTypeId);
    }

    /**
     * ユーザーが閲覧可能なBoxTypeのリストを取得
     */
    public List<BoxType> getAccessibleBoxTypes(Long userId, Long organizationId, Long tenantId) {
        // ユーザーのロールを取得
        Optional<UserRole> userRole = userRoleService.getCurrentRole(userId, organizationId);
        if (userRole.isEmpty()) {
            return List.of();
        }

        Integer roleLevel = userRole.get().getRoleLevel();

        // テナント内のアクティブなBoxTypeを取得し、閲覧可能なものをフィルタリング
        return boxTypeRepository.findAll().stream()
                .filter(boxType -> boxType.getTenantId().equals(tenantId))
                .filter(BoxType::getIsActive)
                .filter(boxType -> roleLevel >= boxType.getMinRoleLevelToView())
                .toList();
    }

    /**
     * ユーザーが投稿可能なBoxTypeのリストを取得
     */
    public List<BoxType> getPostableBoxTypes(Long userId, Long organizationId, Long tenantId) {
        // ユーザーのロールを取得
        Optional<UserRole> userRole = userRoleService.getCurrentRole(userId, organizationId);
        if (userRole.isEmpty()) {
            return List.of();
        }

        Integer roleLevel = userRole.get().getRoleLevel();

        // テナント内のアクティブなBoxTypeを取得し、投稿可能なものをフィルタリング
        return boxTypeRepository.findAll().stream()
                .filter(boxType -> boxType.getTenantId().equals(tenantId))
                .filter(BoxType::getIsActive)
                .filter(boxType -> roleLevel >= boxType.getMinRoleLevelToPost())
                .toList();
    }

    /**
     * BoxPermissionレコードを作成または更新
     */
    public BoxPermission grantPermission(Long userId, Long boxTypeId, Long organizationId,
                                         Long tenantId, boolean canView, boolean canPost,
                                         boolean canReply, boolean canModerate) {
        // 既存のパーミッションを検索
        Optional<BoxPermission> existing = boxPermissionRepository.findAll().stream()
                .filter(bp -> bp.getUserId().equals(userId))
                .filter(bp -> bp.getBoxTypeId().equals(boxTypeId))
                .filter(bp -> bp.getOrganizationId().equals(organizationId))
                .findFirst();

        BoxPermission permission;
        if (existing.isPresent()) {
            permission = existing.get();
        } else {
            permission = new BoxPermission();
            permission.setUserId(userId);
            permission.setBoxTypeId(boxTypeId);
            permission.setOrganizationId(organizationId);
            permission.setTenantId(tenantId);
        }

        permission.setCanView(canView);
        permission.setCanPost(canPost);
        permission.setCanReply(canReply);
        permission.setCanModerate(canModerate);

        return boxPermissionRepository.save(permission);
    }

    /**
     * ユーザーの特定BoxTypeに対する権限を取得
     */
    public Optional<BoxPermission> getUserBoxPermission(Long userId, Long boxTypeId, Long organizationId) {
        return boxPermissionRepository.findAll().stream()
                .filter(bp -> bp.getUserId().equals(userId))
                .filter(bp -> bp.getBoxTypeId().equals(boxTypeId))
                .filter(bp -> bp.getOrganizationId().equals(organizationId))
                .filter(BoxPermission::isValid)
                .findFirst();
    }

    /**
     * ユーザーのロールレベルを取得
     */
    public Integer getUserRoleLevel(Long userId, Long organizationId) {
        Optional<UserRole> userRole = userRoleService.getCurrentRole(userId, organizationId);
        return userRole.map(UserRole::getRoleLevel).orElse(0);
    }

    /**
     * テナント内の全BoxTypeを取得
     */
    public List<BoxType> getAllBoxTypes(Long tenantId) {
        return boxTypeRepository.findAll().stream()
                .filter(boxType -> boxType.getTenantId().equals(tenantId))
                .filter(BoxType::getIsActive)
                .toList();
    }
}
