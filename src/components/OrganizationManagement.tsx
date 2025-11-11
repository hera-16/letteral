import React, { useState, useEffect } from 'react';
import {
  Organization,
  OrganizationMember,
  OrganizationPermissions,
  OrganizationRole,
  organizationManagementService,
  userService,
} from '../services/api';

interface OrganizationManagementProps {
  currentOrganizationId: number;
  currentUserId: number;
}

const OrganizationManagement: React.FC<OrganizationManagementProps> = ({
  currentOrganizationId,
  currentUserId,
}) => {
  const [permissions, setPermissions] = useState<OrganizationPermissions | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // メンバー管理状態
  const [showAddMemberForm, setShowAddMemberForm] = useState(false);
  const [usernameToAdd, setUsernameToAdd] = useState('');
  const [roleToAdd, setRoleToAdd] = useState<OrganizationRole>('MEMBER');

  // 子組織作成状態
  const [showCreateSubOrgForm, setShowCreateSubOrgForm] = useState(false);
  const [subOrgName, setSubOrgName] = useState('');
  const [subOrgDescription, setSubOrgDescription] = useState('');

  useEffect(() => {
    loadPermissions();
  }, [currentOrganizationId]);

  const loadPermissions = async () => {
    try {
      setLoading(true);
      const perms = await organizationManagementService.getPermissions(currentOrganizationId);
      setPermissions(perms);
      setError(null);
    } catch (err: any) {
      setError(err.response?.data?.error || '権限情報の取得に失敗しました');
    } finally {
      setLoading(false);
    }
  };

  const handleAddMember = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await organizationManagementService.addMember(currentOrganizationId, {
        username: usernameToAdd,
        role: roleToAdd,
      });
      setUsernameToAdd('');
      setRoleToAdd('MEMBER');
      setShowAddMemberForm(false);
      alert('メンバーを追加しました');
      loadPermissions();
    } catch (err: any) {
      alert(err.response?.data?.error || 'メンバーの追加に失敗しました');
    }
  };

  const handleCreateSubOrganization = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await organizationManagementService.createSubOrganization(currentOrganizationId, {
        name: subOrgName,
        description: subOrgDescription,
      });
      setSubOrgName('');
      setSubOrgDescription('');
      setShowCreateSubOrgForm(false);
      alert('子組織を作成しました');
      loadPermissions();
    } catch (err: any) {
      alert(err.response?.data?.error || '子組織の作成に失敗しました');
    }
  };

  if (loading) {
    return <div className="p-4">読み込み中...</div>;
  }

  if (error) {
    return <div className="p-4 text-red-600">エラー: {error}</div>;
  }

  if (!permissions) {
    return <div className="p-4">権限情報がありません</div>;
  }

  return (
    <div className="max-w-4xl mx-auto p-6 space-y-6">
      <div className="bg-white rounded-lg shadow p-6">
        <h2 className="text-2xl font-bold mb-4">組織管理</h2>

        {/* 権限情報表示 */}
        <div className="mb-6 p-4 bg-gray-50 rounded">
          <h3 className="font-semibold mb-2">あなたの権限</h3>
          <div className="space-y-1 text-sm">
            <p>役割: <span className="font-medium">{permissions.role || 'なし'}</span></p>
            <p>メンバー追加: {permissions.canAddMember ? '✓ 可能' : '✗ 不可'}</p>
            <p>メンバー削除: {permissions.canRemoveMember ? '✓ 可能' : '✗ 不可'}</p>
            <p>子組織作成: {permissions.canCreateSubOrganization ? '✓ 可能' : '✗ 不可'}</p>
          </div>
        </div>

        {/* メンバー管理 */}
        {permissions.canAddMember && (
          <div className="mb-6">
            <div className="flex items-center justify-between mb-3">
              <h3 className="text-lg font-semibold">メンバー管理</h3>
              <button
                onClick={() => setShowAddMemberForm(!showAddMemberForm)}
                className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
              >
                {showAddMemberForm ? 'キャンセル' : 'メンバーを追加'}
              </button>
            </div>

            {showAddMemberForm && (
              <form onSubmit={handleAddMember} className="p-4 bg-gray-50 rounded space-y-3">
                <div>
                  <label className="block text-sm font-medium mb-1">ユーザー名</label>
                  <input
                    type="text"
                    value={usernameToAdd}
                    onChange={(e) => setUsernameToAdd(e.target.value)}
                    className="w-full px-3 py-2 border rounded"
                    required
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium mb-1">役割</label>
                  <select
                    value={roleToAdd}
                    onChange={(e) => setRoleToAdd(e.target.value as OrganizationRole)}
                    className="w-full px-3 py-2 border rounded"
                  >
                    <option value="MEMBER">一般メンバー</option>
                    <option value="ADMIN_SUPER">スーパー管理者</option>
                    <option value="ADMIN_LEAD">リード管理者</option>
                    <option value="ADMIN_CORE">コア管理者</option>
                    <option value="ADMIN_ROOT">最高権限</option>
                  </select>
                </div>
                <button
                  type="submit"
                  className="w-full px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700"
                >
                  追加
                </button>
              </form>
            )}
          </div>
        )}

        {/* 子組織作成 */}
        {permissions.canCreateSubOrganization && (
          <div className="mb-6">
            <div className="flex items-center justify-between mb-3">
              <h3 className="text-lg font-semibold">子組織作成</h3>
              <button
                onClick={() => setShowCreateSubOrgForm(!showCreateSubOrgForm)}
                className="px-4 py-2 bg-purple-600 text-white rounded hover:bg-purple-700"
              >
                {showCreateSubOrgForm ? 'キャンセル' : '子組織を作成'}
              </button>
            </div>

            {showCreateSubOrgForm && (
              <form onSubmit={handleCreateSubOrganization} className="p-4 bg-gray-50 rounded space-y-3">
                <div>
                  <label className="block text-sm font-medium mb-1">組織名</label>
                  <input
                    type="text"
                    value={subOrgName}
                    onChange={(e) => setSubOrgName(e.target.value)}
                    className="w-full px-3 py-2 border rounded"
                    required
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium mb-1">説明</label>
                  <textarea
                    value={subOrgDescription}
                    onChange={(e) => setSubOrgDescription(e.target.value)}
                    className="w-full px-3 py-2 border rounded"
                    rows={3}
                  />
                </div>
                <button
                  type="submit"
                  className="w-full px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700"
                >
                  作成
                </button>
              </form>
            )}
          </div>
        )}

        {/* 権限がない場合のメッセージ */}
        {!permissions.canAddMember && !permissions.canRemoveMember && !permissions.canCreateSubOrganization && (
          <div className="p-4 bg-yellow-50 border border-yellow-200 rounded">
            <p className="text-yellow-800">
              組織管理機能を使用する権限がありません。管理者にお問い合わせください。
            </p>
          </div>
        )}
      </div>
    </div>
  );
};

export default OrganizationManagement;
