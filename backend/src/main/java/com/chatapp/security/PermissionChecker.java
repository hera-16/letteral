package com.chatapp.security;

import com.chatapp.model.User;
import com.chatapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 権限チェックを行うユーティリティクラス
 * コントローラーやサービスで権限の確認に使用します
 */
@Component
public class PermissionChecker {

    @Autowired
    private UserRepository userRepository;

    /**
     * 現在のユーザーが指定された権限を持っているかチェック
     *
     * @param permission チェックする権限
     * @return 権限を持っている場合true
     */
    public boolean hasPermission(Permission permission) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        return hasPermission(currentUser, permission);
    }

    /**
     * 指定されたユーザーが権限を持っているかチェック
     *
     * @param user ユーザー
     * @param permission チェックする権限
     * @return 権限を持っている場合true
     */
    public boolean hasPermission(User user, Permission permission) {
        if (user == null || user.getRole() == null) {
            return false;
        }

        UserRole role = UserRole.fromString(user.getRole());
        return role.hasPermission(permission);
    }

    /**
     * 現在のユーザーが複数の権限のうちいずれかを持っているかチェック
     *
     * @param permissions チェックする権限のセット
     * @return いずれかの権限を持っている場合true
     */
    public boolean hasAnyPermission(Permission... permissions) {
        for (Permission permission : permissions) {
            if (hasPermission(permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 現在のユーザーがすべての権限を持っているかチェック
     *
     * @param permissions チェックする権限のセット
     * @return すべての権限を持っている場合true
     */
    public boolean hasAllPermissions(Permission... permissions) {
        for (Permission permission : permissions) {
            if (!hasPermission(permission)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 現在のユーザーが指定されたロールを持っているかチェック
     *
     * @param role チェックするロール
     * @return ロールを持っている場合true
     */
    public boolean hasRole(UserRole role) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        return role.name().equals(currentUser.getRole());
    }

    /**
     * 現在のユーザーがいずれかのロールを持っているかチェック
     *
     * @param roles チェックするロールのセット
     * @return いずれかのロールを持っている場合true
     */
    public boolean hasAnyRole(UserRole... roles) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return false;
        }

        String userRole = currentUser.getRole();
        for (UserRole role : roles) {
            if (role.name().equals(userRole)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 現在のユーザーが特定のリソースの所有者かチェック
     *
     * @param resourceOwnerId リソースの所有者ID
     * @return 所有者の場合true
     */
    public boolean isOwner(Long resourceOwnerId) {
        User currentUser = getCurrentUser();
        if (currentUser == null || resourceOwnerId == null) {
            return false;
        }
        return currentUser.getId().equals(resourceOwnerId);
    }

    /**
     * 現在のユーザーが特定のテナントに所属しているかチェック
     *
     * @param tenantId テナントID
     * @return 所属している場合true
     */
    public boolean belongsToTenant(Long tenantId) {
        User currentUser = getCurrentUser();
        if (currentUser == null || tenantId == null) {
            return false;
        }
        return currentUser.getTenantId() != null && currentUser.getTenantId().equals(tenantId);
    }

    /**
     * 現在のユーザーが特定の組織に所属しているかチェック
     *
     * @param organizationId 組織ID
     * @return 所属している場合true
     */
    public boolean belongsToOrganization(Long organizationId) {
        User currentUser = getCurrentUser();
        if (currentUser == null || organizationId == null) {
            return false;
        }
        return currentUser.getPrimaryOrganizationId() != null
            && currentUser.getPrimaryOrganizationId().equals(organizationId);
    }

    /**
     * 現在のユーザーがリソースにアクセスできるかチェック
     * 所有者または権限を持っている場合にアクセス可能
     *
     * @param resourceOwnerId リソースの所有者ID
     * @param permission 必要な権限
     * @return アクセス可能な場合true
     */
    public boolean canAccess(Long resourceOwnerId, Permission permission) {
        return isOwner(resourceOwnerId) || hasPermission(permission);
    }

    /**
     * 現在ログインしているユーザーを取得
     *
     * @return 現在のユーザー、未ログインの場合null
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserPrincipal) {
            UserPrincipal userPrincipal = (UserPrincipal) principal;
            return userRepository.findById(userPrincipal.getId()).orElse(null);
        }

        return null;
    }

    /**
     * 現在のユーザーIDを取得
     *
     * @return 現在のユーザーID、未ログインの場合null
     */
    public Long getCurrentUserId() {
        User currentUser = getCurrentUser();
        return currentUser != null ? currentUser.getId() : null;
    }

    /**
     * 現在のユーザーのテナントIDを取得
     *
     * @return 現在のユーザーのテナントID、未ログインの場合null
     */
    public Long getCurrentTenantId() {
        User currentUser = getCurrentUser();
        return currentUser != null ? currentUser.getTenantId() : null;
    }

    /**
     * 現在のユーザーのロールを取得
     *
     * @return 現在のユーザーのロール、未ログインの場合null
     */
    public UserRole getCurrentUserRole() {
        User currentUser = getCurrentUser();
        if (currentUser == null || currentUser.getRole() == null) {
            return null;
        }
        return UserRole.fromString(currentUser.getRole());
    }

    /**
     * 現在のユーザーの権限セットを取得
     *
     * @return 現在のユーザーの権限セット
     */
    public Set<Permission> getCurrentUserPermissions() {
        UserRole role = getCurrentUserRole();
        if (role == null) {
            return Set.of();
        }
        return role.getPermissions();
    }
}
