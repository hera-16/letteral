import { useMemo } from 'react';
import {
  Permission,
  UserRole,
  hasPermission,
  hasAnyPermission,
  hasAllPermissions,
} from '@/types/permissions';

/**
 * 権限チェック用のカスタムフック
 *
 * @example
 * ```tsx
 * const { can, canAny, canAll, is, isAny } = usePermissions(userRole);
 *
 * if (can(Permission.POST_CREATE)) {
 *   // 投稿作成ボタンを表示
 * }
 *
 * if (is(UserRole.TENANT_ADMIN)) {
 *   // 管理者用メニューを表示
 * }
 * ```
 */
export function usePermissions(currentRole: UserRole) {
  return useMemo(
    () => ({
      /**
       * 特定の権限を持っているかチェック
       */
      can: (permission: Permission) => hasPermission(currentRole, permission),

      /**
       * 複数の権限のいずれかを持っているかチェック
       */
      canAny: (permissions: Permission[]) => hasAnyPermission(currentRole, permissions),

      /**
       * 複数の権限をすべて持っているかチェック
       */
      canAll: (permissions: Permission[]) => hasAllPermissions(currentRole, permissions),

      /**
       * 特定のロールかチェック
       */
      is: (role: UserRole) => currentRole === role,

      /**
       * 複数のロールのいずれかかチェック
       */
      isAny: (roles: UserRole[]) => roles.includes(currentRole),

      /**
       * 現在のロール
       */
      role: currentRole,
    }),
    [currentRole]
  );
}
