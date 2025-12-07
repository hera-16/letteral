'use client';

import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import OrganizationTree from '@/components/OrganizationTree';
import {
  organizationService,
  organizationManagementService,
  OrganizationTreeNode,
  OrganizationMember,
  OrganizationPermissions,
} from '@/services/api';
import { sortMembers, SortOption } from '@/utils/memberSort';
import { LoadingSpinner } from '@/components/ui/LoadingSpinner';
import { useToast } from '@/components/ui/Toast';
import { Users, Building2, Settings } from 'lucide-react';

export default function OrganizationsPage() {
  const router = useRouter();
  const toast = useToast();

  // Tenant ID (TODO: 実際のテナントIDを取得)
  const [tenantId] = useState(1);

  // 組織ツリー
  const [organizationTree, setOrganizationTree] = useState<OrganizationTreeNode[]>([]);
  const [selectedOrgId, setSelectedOrgId] = useState<number | null>(null);

  // メンバー
  const [members, setMembers] = useState<OrganizationMember[]>([]);
  const [sortBy, setSortBy] = useState<SortOption>('role-name');

  // 権限
  const [permissions, setPermissions] = useState<OrganizationPermissions | null>(null);

  // Loading state
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // 組織ツリーを読み込む
  useEffect(() => {
    loadOrganizationTree();
  }, [tenantId]);

  // 選択された組織のメンバーを読み込む
  useEffect(() => {
    if (selectedOrgId) {
      loadMembersAndPermissions();
    }
  }, [selectedOrgId]);

  const loadOrganizationTree = async () => {
    try {
      setLoading(true);
      const tree = await organizationService.getOrganizationTree(tenantId);
      setOrganizationTree(tree);

      // 最初のルート組織を選択
      if (tree.length > 0 && !selectedOrgId) {
        setSelectedOrgId(tree[0].id);
      }

      setError(null);
    } catch (err: any) {
      console.error('組織ツリーの読み込みエラー:', err);
      const errorMessage = '組織ツリーの読み込みに失敗しました';
      setError(errorMessage);
      toast.error(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const loadMembersAndPermissions = async () => {
    if (!selectedOrgId) return;

    try {
      const [membersList, perms] = await Promise.all([
        organizationManagementService.getMembers(selectedOrgId),
        organizationManagementService.getPermissions(selectedOrgId),
      ]);

      setMembers(membersList);
      setPermissions(perms);
    } catch (err: any) {
      console.error('メンバーと権限の読み込みエラー:', err);
      toast.error('メンバー情報の読み込みに失敗しました');
    }
  };

  const getRoleLabel = (role: string) => {
    const labels: Record<string, string> = {
      ADMIN_ROOT: '最高権限',
      ADMIN_CORE: 'コア管理者',
      ADMIN_LEAD: 'リード管理者',
      ADMIN_SUPER: 'スーパー管理者',
      MEMBER: '一般メンバー',
    };
    return labels[role] || role;
  };

  const getRoleBadgeColor = (role: string) => {
    const colors: Record<string, string> = {
      ADMIN_ROOT: 'bg-red-100 text-red-800',
      ADMIN_CORE: 'bg-orange-100 text-orange-800',
      ADMIN_LEAD: 'bg-yellow-100 text-yellow-800',
      ADMIN_SUPER: 'bg-blue-100 text-blue-800',
      MEMBER: 'bg-gray-100 text-gray-800',
    };
    return colors[role] || 'bg-gray-100 text-gray-800';
  };

  // ソートされたメンバー一覧
  const sortedMembers = sortMembers(members, sortBy);

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <LoadingSpinner />
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="bg-red-50 border border-red-200 rounded-lg p-4 text-red-800">
          {error}
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* ヘッダー */}
      <div className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <div className="flex justify-between items-center">
            <div>
              <h1 className="text-3xl font-bold text-gray-900">組織管理</h1>
              <p className="mt-1 text-sm text-gray-500">
                組織階層とメンバー管理
              </p>
            </div>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* 組織ツリー */}
          <div className="lg:col-span-1">
            <div className="bg-white rounded-lg shadow p-6">
              <div className="flex items-center gap-2 mb-4">
                <Building2 className="w-5 h-5 text-gray-700" />
                <h2 className="text-xl font-bold text-gray-900">組織ツリー</h2>
              </div>
              <OrganizationTree
                nodes={organizationTree}
                onSelectOrganization={setSelectedOrgId}
                selectedOrganizationId={selectedOrgId}
              />
            </div>
          </div>

          {/* メンバー一覧 */}
          <div className="lg:col-span-2">
            {selectedOrgId ? (
              <div className="bg-white rounded-lg shadow p-6">
                <div className="flex items-center justify-between mb-6">
                  <div className="flex items-center gap-2">
                    <Users className="w-5 h-5 text-gray-700" />
                    <h2 className="text-xl font-bold text-gray-900">
                      メンバー一覧 ({sortedMembers.length}人)
                    </h2>
                  </div>

                  {/* ソート選択 */}
                  <div className="flex items-center gap-2">
                    <label className="text-sm text-gray-600">ソート:</label>
                    <select
                      value={sortBy}
                      onChange={(e) => setSortBy(e.target.value as SortOption)}
                      className="px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                    >
                      <option value="role-name">権限階級順 → あいうえお順</option>
                      <option value="role">権限階級順のみ</option>
                      <option value="name">名前順のみ</option>
                    </select>
                  </div>
                </div>

                {/* 権限情報 */}
                {permissions && (
                  <div className="mb-6 p-4 bg-gray-50 rounded-lg">
                    <h3 className="text-sm font-semibold text-gray-700 mb-2">
                      あなたの権限
                    </h3>
                    <div className="text-sm text-gray-600">
                      <p>役割: <span className="font-medium">{permissions.role ? getRoleLabel(permissions.role) : 'なし'}</span></p>
                    </div>
                  </div>
                )}

                {/* メンバーリスト */}
                {sortedMembers.length === 0 ? (
                  <div className="text-center py-12 text-gray-500">
                    メンバーがいません
                  </div>
                ) : (
                  <div className="space-y-2">
                    {sortedMembers.map((member) => (
                      <div
                        key={member.id}
                        className="flex items-center justify-between p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors"
                      >
                        <div>
                          <p className="font-medium text-gray-900">
                            {member.displayName || member.username}
                          </p>
                          <p className="text-sm text-gray-500">
                            @{member.username}
                          </p>
                        </div>

                        <span
                          className={`px-3 py-1 text-xs font-medium rounded-full ${getRoleBadgeColor(
                            member.role
                          )}`}
                        >
                          {getRoleLabel(member.role)}
                        </span>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            ) : (
              <div className="bg-white rounded-lg shadow p-12 text-center">
                <Building2 className="w-16 h-16 text-gray-300 mx-auto mb-4" />
                <p className="text-gray-500">
                  左側から組織を選択してください
                </p>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
