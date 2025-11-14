/**
 * システム権限の定義
 * バックエンドのPermission enumと対応
 */
export enum Permission {
  // テナント管理権限
  TENANT_VIEW = 'TENANT_VIEW',
  TENANT_CREATE = 'TENANT_CREATE',
  TENANT_UPDATE = 'TENANT_UPDATE',
  TENANT_DELETE = 'TENANT_DELETE',
  TENANT_MANAGE_PLAN = 'TENANT_MANAGE_PLAN',
  TENANT_MANAGE_SETTINGS = 'TENANT_MANAGE_SETTINGS',

  // ユーザー管理権限
  USER_VIEW = 'USER_VIEW',
  USER_VIEW_ALL = 'USER_VIEW_ALL',
  USER_CREATE = 'USER_CREATE',
  USER_UPDATE = 'USER_UPDATE',
  USER_DELETE = 'USER_DELETE',
  USER_MANAGE_ROLE = 'USER_MANAGE_ROLE',
  USER_INVITE = 'USER_INVITE',

  // 組織管理権限
  ORG_VIEW = 'ORG_VIEW',
  ORG_CREATE = 'ORG_CREATE',
  ORG_UPDATE = 'ORG_UPDATE',
  ORG_DELETE = 'ORG_DELETE',
  ORG_MANAGE_MEMBERS = 'ORG_MANAGE_MEMBERS',
  ORG_MANAGE_HIERARCHY = 'ORG_MANAGE_HIERARCHY',

  // グループ管理権限
  GROUP_VIEW = 'GROUP_VIEW',
  GROUP_CREATE = 'GROUP_CREATE',
  GROUP_UPDATE = 'GROUP_UPDATE',
  GROUP_DELETE = 'GROUP_DELETE',
  GROUP_MANAGE_MEMBERS = 'GROUP_MANAGE_MEMBERS',

  // 進捗投稿権限
  POST_VIEW = 'POST_VIEW',
  POST_VIEW_ALL = 'POST_VIEW_ALL',
  POST_CREATE = 'POST_CREATE',
  POST_UPDATE = 'POST_UPDATE',
  POST_DELETE = 'POST_DELETE',
  POST_MODERATE = 'POST_MODERATE',

  // OKR権限
  OKR_VIEW = 'OKR_VIEW',
  OKR_VIEW_ALL = 'OKR_VIEW_ALL',
  OKR_CREATE = 'OKR_CREATE',
  OKR_UPDATE = 'OKR_UPDATE',
  OKR_DELETE = 'OKR_DELETE',
  OKR_MANAGE_ORG = 'OKR_MANAGE_ORG',

  // チャット権限
  CHAT_VIEW = 'CHAT_VIEW',
  CHAT_SEND = 'CHAT_SEND',
  CHAT_MODERATE = 'CHAT_MODERATE',
  CHAT_DELETE = 'CHAT_DELETE',

  // ポリシー・設定権限
  POLICY_VIEW = 'POLICY_VIEW',
  POLICY_UPDATE = 'POLICY_UPDATE',
  SETTINGS_VIEW = 'SETTINGS_VIEW',
  SETTINGS_UPDATE = 'SETTINGS_UPDATE',

  // レポート・評価権限
  REPORT_VIEW = 'REPORT_VIEW',
  REPORT_VIEW_ALL = 'REPORT_VIEW_ALL',
  REPORT_EXPORT = 'REPORT_EXPORT',
  EVALUATION_VIEW = 'EVALUATION_VIEW',
  EVALUATION_CREATE = 'EVALUATION_CREATE',

  // 監査ログ権限
  AUDIT_VIEW = 'AUDIT_VIEW',
  AUDIT_EXPORT = 'AUDIT_EXPORT',

  // システム管理権限
  SYSTEM_ADMIN = 'SYSTEM_ADMIN',
}

/**
 * ユーザーロールの定義
 * バックエンドのUserRole enumと対応
 */
export enum UserRole {
  SUPER_ADMIN = 'SUPER_ADMIN',
  TENANT_ADMIN = 'TENANT_ADMIN',
  ORG_ADMIN = 'ORG_ADMIN',
  MODERATOR = 'MODERATOR',
  USER = 'USER',
}

/**
 * 各ロールが持つ権限のマッピング
 */
export const RolePermissions: Record<UserRole, Permission[]> = {
  [UserRole.SUPER_ADMIN]: Object.values(Permission),
  [UserRole.TENANT_ADMIN]: [
    Permission.TENANT_VIEW,
    Permission.TENANT_UPDATE,
    Permission.TENANT_MANAGE_PLAN,
    Permission.TENANT_MANAGE_SETTINGS,
    Permission.USER_VIEW,
    Permission.USER_VIEW_ALL,
    Permission.USER_CREATE,
    Permission.USER_UPDATE,
    Permission.USER_DELETE,
    Permission.USER_MANAGE_ROLE,
    Permission.USER_INVITE,
    Permission.ORG_VIEW,
    Permission.ORG_CREATE,
    Permission.ORG_UPDATE,
    Permission.ORG_DELETE,
    Permission.ORG_MANAGE_MEMBERS,
    Permission.ORG_MANAGE_HIERARCHY,
    Permission.GROUP_VIEW,
    Permission.GROUP_CREATE,
    Permission.GROUP_UPDATE,
    Permission.GROUP_DELETE,
    Permission.GROUP_MANAGE_MEMBERS,
    Permission.POST_VIEW,
    Permission.POST_VIEW_ALL,
    Permission.POST_CREATE,
    Permission.POST_UPDATE,
    Permission.POST_DELETE,
    Permission.POST_MODERATE,
    Permission.OKR_VIEW,
    Permission.OKR_VIEW_ALL,
    Permission.OKR_CREATE,
    Permission.OKR_UPDATE,
    Permission.OKR_DELETE,
    Permission.OKR_MANAGE_ORG,
    Permission.CHAT_VIEW,
    Permission.CHAT_SEND,
    Permission.CHAT_MODERATE,
    Permission.CHAT_DELETE,
    Permission.POLICY_VIEW,
    Permission.POLICY_UPDATE,
    Permission.SETTINGS_VIEW,
    Permission.SETTINGS_UPDATE,
    Permission.REPORT_VIEW,
    Permission.REPORT_VIEW_ALL,
    Permission.REPORT_EXPORT,
    Permission.EVALUATION_VIEW,
    Permission.EVALUATION_CREATE,
    Permission.AUDIT_VIEW,
    Permission.AUDIT_EXPORT,
  ],
  [UserRole.ORG_ADMIN]: [
    Permission.TENANT_VIEW,
    Permission.USER_VIEW,
    Permission.USER_INVITE,
    Permission.ORG_VIEW,
    Permission.ORG_UPDATE,
    Permission.ORG_MANAGE_MEMBERS,
    Permission.GROUP_VIEW,
    Permission.GROUP_CREATE,
    Permission.GROUP_UPDATE,
    Permission.GROUP_DELETE,
    Permission.GROUP_MANAGE_MEMBERS,
    Permission.POST_VIEW,
    Permission.POST_VIEW_ALL,
    Permission.POST_CREATE,
    Permission.POST_UPDATE,
    Permission.POST_DELETE,
    Permission.POST_MODERATE,
    Permission.OKR_VIEW,
    Permission.OKR_VIEW_ALL,
    Permission.OKR_CREATE,
    Permission.OKR_UPDATE,
    Permission.OKR_DELETE,
    Permission.OKR_MANAGE_ORG,
    Permission.CHAT_VIEW,
    Permission.CHAT_SEND,
    Permission.CHAT_MODERATE,
    Permission.POLICY_VIEW,
    Permission.REPORT_VIEW,
    Permission.REPORT_VIEW_ALL,
    Permission.REPORT_EXPORT,
    Permission.EVALUATION_VIEW,
  ],
  [UserRole.MODERATOR]: [
    Permission.TENANT_VIEW,
    Permission.USER_VIEW,
    Permission.ORG_VIEW,
    Permission.GROUP_VIEW,
    Permission.GROUP_CREATE,
    Permission.GROUP_UPDATE,
    Permission.GROUP_MANAGE_MEMBERS,
    Permission.POST_VIEW,
    Permission.POST_VIEW_ALL,
    Permission.POST_CREATE,
    Permission.POST_UPDATE,
    Permission.POST_MODERATE,
    Permission.OKR_VIEW,
    Permission.OKR_VIEW_ALL,
    Permission.OKR_CREATE,
    Permission.OKR_UPDATE,
    Permission.CHAT_VIEW,
    Permission.CHAT_SEND,
    Permission.CHAT_MODERATE,
    Permission.POLICY_VIEW,
    Permission.REPORT_VIEW,
  ],
  [UserRole.USER]: [
    Permission.TENANT_VIEW,
    Permission.USER_VIEW,
    Permission.ORG_VIEW,
    Permission.GROUP_VIEW,
    Permission.GROUP_CREATE,
    Permission.POST_VIEW,
    Permission.POST_CREATE,
    Permission.POST_UPDATE,
    Permission.POST_DELETE,
    Permission.OKR_VIEW,
    Permission.OKR_CREATE,
    Permission.OKR_UPDATE,
    Permission.OKR_DELETE,
    Permission.CHAT_VIEW,
    Permission.CHAT_SEND,
    Permission.REPORT_VIEW,
    Permission.REPORT_EXPORT,
  ],
};

/**
 * ロールが特定の権限を持っているかチェック
 */
export function hasPermission(role: UserRole, permission: Permission): boolean {
  return RolePermissions[role]?.includes(permission) ?? false;
}

/**
 * ロールが複数の権限のいずれかを持っているかチェック
 */
export function hasAnyPermission(role: UserRole, permissions: Permission[]): boolean {
  return permissions.some((permission) => hasPermission(role, permission));
}

/**
 * ロールが複数の権限をすべて持っているかチェック
 */
export function hasAllPermissions(role: UserRole, permissions: Permission[]): boolean {
  return permissions.every((permission) => hasPermission(role, permission));
}
