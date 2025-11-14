package com.chatapp.security;

import java.util.Set;
import java.util.EnumSet;
import static com.chatapp.security.Permission.*;

/**
 * ユーザーロールと権限のマッピング
 * 各ロールに付与される権限セットを定義します
 */
public enum UserRole {

    /**
     * スーパー管理者
     * システム全体を管理できる最上位の権限
     */
    SUPER_ADMIN(EnumSet.allOf(Permission.class)),

    /**
     * テナント管理者
     * テナント内のすべてのリソースと設定を管理できる
     */
    TENANT_ADMIN(EnumSet.of(
        // テナント管理（削除以外）
        TENANT_VIEW, TENANT_UPDATE, TENANT_MANAGE_PLAN, TENANT_MANAGE_SETTINGS,

        // ユーザー管理（すべて）
        USER_VIEW, USER_VIEW_ALL, USER_CREATE, USER_UPDATE, USER_DELETE,
        USER_MANAGE_ROLE, USER_INVITE,

        // 組織管理（すべて）
        ORG_VIEW, ORG_CREATE, ORG_UPDATE, ORG_DELETE, ORG_MANAGE_MEMBERS, ORG_MANAGE_HIERARCHY,

        // グループ管理（すべて）
        GROUP_VIEW, GROUP_CREATE, GROUP_UPDATE, GROUP_DELETE, GROUP_MANAGE_MEMBERS,

        // 投稿管理
        POST_VIEW, POST_VIEW_ALL, POST_CREATE, POST_UPDATE, POST_DELETE, POST_MODERATE,

        // OKR管理
        OKR_VIEW, OKR_VIEW_ALL, OKR_CREATE, OKR_UPDATE, OKR_DELETE, OKR_MANAGE_ORG,

        // チャット管理
        CHAT_VIEW, CHAT_SEND, CHAT_MODERATE, CHAT_DELETE,

        // ポリシー・設定管理
        POLICY_VIEW, POLICY_UPDATE, SETTINGS_VIEW, SETTINGS_UPDATE,

        // レポート・評価
        REPORT_VIEW, REPORT_VIEW_ALL, REPORT_EXPORT, EVALUATION_VIEW, EVALUATION_CREATE,

        // 監査ログ
        AUDIT_VIEW, AUDIT_EXPORT
    )),

    /**
     * 組織管理者
     * 特定の組織内のリソースを管理できる
     */
    ORG_ADMIN(EnumSet.of(
        // テナント閲覧のみ
        TENANT_VIEW,

        // ユーザー管理（限定）
        USER_VIEW, USER_INVITE,

        // 組織管理（自組織のみ）
        ORG_VIEW, ORG_UPDATE, ORG_MANAGE_MEMBERS,

        // グループ管理
        GROUP_VIEW, GROUP_CREATE, GROUP_UPDATE, GROUP_DELETE, GROUP_MANAGE_MEMBERS,

        // 投稿管理
        POST_VIEW, POST_VIEW_ALL, POST_CREATE, POST_UPDATE, POST_DELETE, POST_MODERATE,

        // OKR管理
        OKR_VIEW, OKR_VIEW_ALL, OKR_CREATE, OKR_UPDATE, OKR_DELETE, OKR_MANAGE_ORG,

        // チャット管理
        CHAT_VIEW, CHAT_SEND, CHAT_MODERATE,

        // ポリシー閲覧
        POLICY_VIEW,

        // レポート
        REPORT_VIEW, REPORT_VIEW_ALL, REPORT_EXPORT, EVALUATION_VIEW
    )),

    /**
     * モデレーター
     * コンテンツのモデレーションができる
     */
    MODERATOR(EnumSet.of(
        // テナント閲覧
        TENANT_VIEW,

        // ユーザー閲覧
        USER_VIEW,

        // 組織閲覧
        ORG_VIEW,

        // グループ管理
        GROUP_VIEW, GROUP_CREATE, GROUP_UPDATE, GROUP_MANAGE_MEMBERS,

        // 投稿モデレーション
        POST_VIEW, POST_VIEW_ALL, POST_CREATE, POST_UPDATE, POST_MODERATE,

        // OKR閲覧・作成
        OKR_VIEW, OKR_VIEW_ALL, OKR_CREATE, OKR_UPDATE,

        // チャットモデレーション
        CHAT_VIEW, CHAT_SEND, CHAT_MODERATE,

        // ポリシー閲覧
        POLICY_VIEW,

        // レポート閲覧
        REPORT_VIEW
    )),

    /**
     * 一般ユーザー
     * 基本的な機能のみ利用できる
     */
    USER(EnumSet.of(
        // テナント閲覧
        TENANT_VIEW,

        // 自分の情報のみ
        USER_VIEW,

        // 組織閲覧
        ORG_VIEW,

        // グループ参加・閲覧
        GROUP_VIEW, GROUP_CREATE,

        // 投稿の基本操作
        POST_VIEW, POST_CREATE, POST_UPDATE, POST_DELETE,

        // OKRの基本操作
        OKR_VIEW, OKR_CREATE, OKR_UPDATE, OKR_DELETE,

        // チャット基本操作
        CHAT_VIEW, CHAT_SEND,

        // レポート閲覧（自分のみ）
        REPORT_VIEW, REPORT_EXPORT
    ));

    private final Set<Permission> permissions;

    UserRole(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    public Set<Permission> getPermissions() {
        return EnumSet.copyOf(permissions);
    }

    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission);
    }

    /**
     * ロール名から UserRole を取得
     */
    public static UserRole fromString(String role) {
        try {
            return UserRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            return UserRole.USER; // デフォルトは一般ユーザー
        }
    }
}
