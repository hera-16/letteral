package com.chatapp.model.enums;

/**
 * 組織内での階層的役割
 * 各役割は下位の役割に対して管理権限を持つ
 */
public enum OrganizationRole {
    // システムオーナー（最高権限）
    OWNER,

    // 階層的管理者役割
    ADMIN_ROOT,    // 役員・社長 - 会社全体の管理（部・支店レベル）メンバー追加削除、部署階層グループ追加、ADMIN_ROOTまでの権限付与
    ADMIN_CORE,    // 部長 - 部門管理（課・グループレベル）メンバー追加削除、課階層グループ追加、ADMIN_COREまでの権限付与
    ADMIN_LEAD,    // 課長 - 課管理（チームレベル）メンバー追加削除、チーム階層グループ追加、ADMIN_LEADまでの権限付与
    ADMIN_SUPER,   // PM - チーム管理（プロジェクトレベル）メンバー追加削除、プロジェクトチーム階層グループ追加、ADMIN_SUPERまでの権限付与

    // 通常メンバー
    MEMBER;        // メンバー - 閲覧・投稿のみ

    /**
     * この役割が指定された役割以上の権限を持つかチェック
     */
    public boolean hasAuthorityOver(OrganizationRole targetRole) {
        if (this == OWNER) return true;
        if (targetRole == OWNER) return false;
        return this.ordinal() <= targetRole.ordinal();
    }

    /**
     * この役割が指定された役割を付与できるかチェック
     */
    public boolean canAssignRole(OrganizationRole targetRole) {
        if (this == OWNER) return true;
        if (targetRole == OWNER) return false;
        // ADMIN_ROOTはADMIN_ROOTまで、ADMIN_COREはADMIN_COREまで付与可能
        return this.ordinal() <= targetRole.ordinal();
    }
}
