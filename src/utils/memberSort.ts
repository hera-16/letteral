import { OrganizationMember, OrganizationRole } from '../services/api';

/**
 * 組織ロールの優先順位を定義
 * 数値が小さいほど権限が高い
 */
const ROLE_PRIORITY: Record<OrganizationRole, number> = {
  ADMIN_ROOT: 1,
  ADMIN_CORE: 2,
  ADMIN_LEAD: 3,
  ADMIN_SUPER: 4,
  MEMBER: 5,
};

/**
 * メンバーを権限階級順にソート
 */
export function sortMembersByRole(members: OrganizationMember[]): OrganizationMember[] {
  return [...members].sort((a, b) => {
    const priorityA = ROLE_PRIORITY[a.role];
    const priorityB = ROLE_PRIORITY[b.role];
    return priorityA - priorityB;
  });
}

/**
 * メンバーを名前順（あいうえお順）にソート
 */
export function sortMembersByName(members: OrganizationMember[]): OrganizationMember[] {
  return [...members].sort((a, b) => {
    const nameA = a.displayName || a.username;
    const nameB = b.displayName || b.username;
    return nameA.localeCompare(nameB, 'ja');
  });
}

/**
 * メンバーを権限階級順 → あいうえお順にソート
 */
export function sortMembersByRoleThenName(members: OrganizationMember[]): OrganizationMember[] {
  return [...members].sort((a, b) => {
    // まず権限階級でソート
    const priorityA = ROLE_PRIORITY[a.role];
    const priorityB = ROLE_PRIORITY[b.role];

    if (priorityA !== priorityB) {
      return priorityA - priorityB;
    }

    // 同じ権限階級の場合は名前でソート
    const nameA = a.displayName || a.username;
    const nameB = b.displayName || b.username;
    return nameA.localeCompare(nameB, 'ja');
  });
}

export type SortOption = 'role' | 'name' | 'role-name';

/**
 * ソートオプションに応じてメンバーをソート
 */
export function sortMembers(
  members: OrganizationMember[],
  sortBy: SortOption
): OrganizationMember[] {
  switch (sortBy) {
    case 'role':
      return sortMembersByRole(members);
    case 'name':
      return sortMembersByName(members);
    case 'role-name':
      return sortMembersByRoleThenName(members);
    default:
      return members;
  }
}
