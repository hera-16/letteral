import { ReactNode } from 'react';
import { Permission, UserRole, hasPermission, hasAnyPermission, hasAllPermissions } from '@/types/permissions';

interface PermissionGuardProps {
  children: ReactNode;
  /** 必要な権限（単一） */
  permission?: Permission;
  /** 必要な権限（複数のいずれか） */
  anyPermissions?: Permission[];
  /** 必要な権限（すべて） */
  allPermissions?: Permission[];
  /** 必要なロール */
  role?: UserRole;
  /** 必要なロール（複数のいずれか） */
  anyRoles?: UserRole[];
  /** 現在のユーザーロール（propsまたはcontextから取得） */
  currentRole: UserRole;
  /** 権限がない場合に表示するコンテンツ */
  fallback?: ReactNode;
}

/**
 * 権限に基づいてUIコンポーネントの表示を制御するガードコンポーネント
 *
 * @example
 * ```tsx
 * <PermissionGuard
 *   permission={Permission.POST_CREATE}
 *   currentRole={user.role}
 * >
 *   <button>投稿を作成</button>
 * </PermissionGuard>
 * ```
 *
 * @example
 * ```tsx
 * <PermissionGuard
 *   anyPermissions={[Permission.POST_UPDATE, Permission.POST_DELETE]}
 *   currentRole={user.role}
 *   fallback={<span>権限がありません</span>}
 * >
 *   <button>編集</button>
 * </PermissionGuard>
 * ```
 */
export function PermissionGuard({
  children,
  permission,
  anyPermissions,
  allPermissions,
  role,
  anyRoles,
  currentRole,
  fallback = null,
}: PermissionGuardProps) {
  // ロールチェック
  if (role && currentRole !== role) {
    return <>{fallback}</>;
  }

  if (anyRoles && !anyRoles.includes(currentRole)) {
    return <>{fallback}</>;
  }

  // 権限チェック
  if (permission && !hasPermission(currentRole, permission)) {
    return <>{fallback}</>;
  }

  if (anyPermissions && !hasAnyPermission(currentRole, anyPermissions)) {
    return <>{fallback}</>;
  }

  if (allPermissions && !hasAllPermissions(currentRole, allPermissions)) {
    return <>{fallback}</>;
  }

  return <>{children}</>;
}
