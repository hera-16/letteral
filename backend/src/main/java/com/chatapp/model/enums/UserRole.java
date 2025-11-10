package com.chatapp.model.enums;

/**
 * ユーザーの役割
 */
public enum UserRole {
    SUPER_ADMIN,   // スーパー管理者
    TENANT_ADMIN,  // テナント管理者
    ORG_ADMIN,     // 組織管理者
    MODERATOR,     // モデレーター
    USER           // 一般ユーザー
}
