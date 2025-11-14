package com.chatapp.security;

/**
 * システム権限の定義
 * 各ロールに対して付与される権限を細かく定義します
 */
public enum Permission {

    // ============ テナント管理権限 ============
    TENANT_VIEW("テナント情報の閲覧"),
    TENANT_CREATE("テナント作成"),
    TENANT_UPDATE("テナント情報の更新"),
    TENANT_DELETE("テナント削除"),
    TENANT_MANAGE_PLAN("テナントプランの管理"),
    TENANT_MANAGE_SETTINGS("テナント設定の管理"),

    // ============ ユーザー管理権限 ============
    USER_VIEW("ユーザー情報の閲覧"),
    USER_VIEW_ALL("全ユーザー情報の閲覧"),
    USER_CREATE("ユーザー作成"),
    USER_UPDATE("ユーザー情報の更新"),
    USER_DELETE("ユーザー削除"),
    USER_MANAGE_ROLE("ユーザーロールの管理"),
    USER_INVITE("ユーザー招待"),

    // ============ 組織管理権限 ============
    ORG_VIEW("組織情報の閲覧"),
    ORG_CREATE("組織作成"),
    ORG_UPDATE("組織情報の更新"),
    ORG_DELETE("組織削除"),
    ORG_MANAGE_MEMBERS("組織メンバーの管理"),
    ORG_MANAGE_HIERARCHY("組織階層の管理"),

    // ============ グループ管理権限 ============
    GROUP_VIEW("グループ閲覧"),
    GROUP_CREATE("グループ作成"),
    GROUP_UPDATE("グループ更新"),
    GROUP_DELETE("グループ削除"),
    GROUP_MANAGE_MEMBERS("グループメンバー管理"),

    // ============ 進捗投稿権限 ============
    POST_VIEW("投稿閲覧"),
    POST_VIEW_ALL("全投稿閲覧"),
    POST_CREATE("投稿作成"),
    POST_UPDATE("投稿更新"),
    POST_DELETE("投稿削除"),
    POST_MODERATE("投稿モデレーション"),

    // ============ OKR権限 ============
    OKR_VIEW("OKR閲覧"),
    OKR_VIEW_ALL("全OKR閲覧"),
    OKR_CREATE("OKR作成"),
    OKR_UPDATE("OKR更新"),
    OKR_DELETE("OKR削除"),
    OKR_MANAGE_ORG("組織OKR管理"),

    // ============ チャット権限 ============
    CHAT_VIEW("チャット閲覧"),
    CHAT_SEND("チャット送信"),
    CHAT_MODERATE("チャットモデレーション"),
    CHAT_DELETE("チャット削除"),

    // ============ ポリシー・設定権限 ============
    POLICY_VIEW("ポリシー閲覧"),
    POLICY_UPDATE("ポリシー更新"),
    SETTINGS_VIEW("設定閲覧"),
    SETTINGS_UPDATE("設定更新"),

    // ============ レポート・評価権限 ============
    REPORT_VIEW("レポート閲覧"),
    REPORT_VIEW_ALL("全レポート閲覧"),
    REPORT_EXPORT("レポートエクスポート"),
    EVALUATION_VIEW("評価情報閲覧"),
    EVALUATION_CREATE("評価作成"),

    // ============ 監査ログ権限 ============
    AUDIT_VIEW("監査ログ閲覧"),
    AUDIT_EXPORT("監査ログエクスポート"),

    // ============ システム管理権限 ============
    SYSTEM_ADMIN("システム管理");

    private final String description;

    Permission(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
