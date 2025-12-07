import React, { useState, useEffect } from 'react';
import {
  Organization,
  OrganizationMember,
  OrganizationPermissions,
  OrganizationRole,
  organizationManagementService,
  userService,
} from '../services/api';
import OrganizationTree, { OrganizationTreeNode } from './OrganizationTree';
import { organizationService } from '../services/api';

interface OrganizationManagementProps {
  currentOrganizationId: number;
  currentUserId: number;
}

const OrganizationManagement: React.FC<OrganizationManagementProps> = ({
  currentOrganizationId,
  currentUserId,
}) => {
  const [permissions, setPermissions] = useState<OrganizationPermissions | null>(null);
  const [members, setMembers] = useState<OrganizationMember[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [orgTree, setOrgTree] = useState<OrganizationTreeNode | null>(null);

  // 選択された組織の状態
  const [selectedOrgId, setSelectedOrgId] = useState<number | null>(null);
  const [selectedOrgPermissions, setSelectedOrgPermissions] = useState<OrganizationPermissions | null>(null);
  const [selectedOrgMembers, setSelectedOrgMembers] = useState<OrganizationMember[]>([]);

  // メンバー管理状態
  const [showAddMemberForm, setShowAddMemberForm] = useState(false);
  const [usernameToAdd, setUsernameToAdd] = useState('');
  const [roleToAdd, setRoleToAdd] = useState<OrganizationRole>('MEMBER');

  // ソート状態
  const [sortBy, setSortBy] = useState<'name' | 'role' | 'joinedAt'>('joinedAt');
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('desc');

  // 子組織作成状態
  const [showCreateSubOrgForm, setShowCreateSubOrgForm] = useState(false);
  const [subOrgName, setSubOrgName] = useState('');
  const [subOrgDescription, setSubOrgDescription] = useState('');

  useEffect(() => {
    loadData();
  }, [currentOrganizationId]);

  useEffect(() => {
    if (selectedOrgId) {
      loadSelectedOrgData();
    }
  }, [selectedOrgId, sortBy, sortOrder]);

  const loadData = async () => {
    try {
      setLoading(true);
      const [perms, memberList, tree] = await Promise.all([
        organizationManagementService.getPermissions(currentOrganizationId),
        organizationManagementService.getMembers(currentOrganizationId, sortBy, sortOrder),
        organizationService.getOrganizationSubTree(currentOrganizationId)
      ]);
      setPermissions(perms);
      setMembers(memberList);
      setOrgTree(tree);
      setError(null);
    } catch (err: any) {
      setError(err.response?.data?.error || 'データの取得に失敗しました');
    } finally {
      setLoading(false);
    }
  };

  const loadSelectedOrgData = async () => {
    if (!selectedOrgId) return;

    try {
      // 権限情報を取得
      const perms = await organizationManagementService.getPermissions(selectedOrgId);
      setSelectedOrgPermissions(perms);

      // メンバー情報を取得
      try {
        const memberList = await organizationManagementService.getMembers(selectedOrgId, sortBy, sortOrder);
        setSelectedOrgMembers(memberList);
        setError(null);
      } catch (memberErr: any) {
        // 403エラーは想定される動作（閲覧権限がない）
        if (memberErr.response?.status === 403) {
          console.log('この組織のメンバーを閲覧する権限がありません（通常動作）');
          setSelectedOrgMembers([]);
          setError(null);
        } else {
          console.error('メンバー情報の取得に失敗:', memberErr);
          setSelectedOrgMembers([]);
          setError('メンバー情報の取得に失敗しました');
        }
      }
    } catch (err: any) {
      console.error('選択された組織のデータ取得に失敗:', err);
      // 権限エラーの場合は空の権限情報を設定
      if (err.response?.status === 403) {
        setSelectedOrgPermissions({
          isMember: false,
          role: null,
          canAddMember: false,
          canRemoveMember: false,
          canCreateSubOrganization: false,
        });
        setSelectedOrgMembers([]);
        setError(null);
      } else {
        setError(err.response?.data?.error || '組織情報の取得に失敗しました');
      }
    }
  };

  const handleSelectOrganization = (orgId: number) => {
    setSelectedOrgId(orgId);
    setShowAddMemberForm(false);
    setShowCreateSubOrgForm(false);
  };

  const handleCloseEditPanel = () => {
    setSelectedOrgId(null);
    setSelectedOrgPermissions(null);
    setSelectedOrgMembers([]);
    setShowAddMemberForm(false);
    setShowCreateSubOrgForm(false);
  };

  const handleAddMember = async (e: React.FormEvent) => {
    e.preventDefault();
    const targetOrgId = selectedOrgId || currentOrganizationId;
    console.log('handleAddMember called', { usernameToAdd, roleToAdd, targetOrgId });

    if (!usernameToAdd.trim()) {
      alert('ユーザー名を入力してください');
      return;
    }

    try {
      console.log('Calling addMember API...');
      await organizationManagementService.addMember(targetOrgId, {
        username: usernameToAdd,
        role: roleToAdd,
      });
      console.log('API call successful');
      setUsernameToAdd('');
      setRoleToAdd('MEMBER');
      setShowAddMemberForm(false);
      alert('メンバーを追加しました');
      if (selectedOrgId) {
        loadSelectedOrgData();
      } else {
        loadData();
      }
    } catch (err: any) {
      console.error('Error adding member:', err);
      alert(err.response?.data?.error || 'メンバーの追加に失敗しました');
    }
  };

  const handleCreateSubOrganization = async (e: React.FormEvent) => {
    e.preventDefault();
    const targetOrgId = selectedOrgId || currentOrganizationId;
    try {
      await organizationManagementService.createSubOrganization(targetOrgId, {
        name: subOrgName,
        description: subOrgDescription,
      });
      setSubOrgName('');
      setSubOrgDescription('');
      setShowCreateSubOrgForm(false);
      alert('子組織を作成しました');
      loadData();
      if (selectedOrgId) {
        loadSelectedOrgData();
      }
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

  // 編集パネル用のデータと権限
  const editPanelPermissions = selectedOrgPermissions || permissions;
  const editPanelMembers = selectedOrgId ? selectedOrgMembers : members;

  // デバッグ用
  console.log('Render state:', {
    selectedOrgId,
    selectedOrgMembers: selectedOrgMembers.length,
    members: members.length,
    editPanelMembers: editPanelMembers.length
  });

  return (
    <div className="w-full h-full p-6">
      <h2 className="text-2xl font-bold mb-6 text-gray-900">組織管理</h2>

      <div className={`flex gap-6 ${selectedOrgId ? 'h-[calc(100vh-12rem)]' : 'justify-center'}`}>
        {/* 組織階層ツリー */}
        <div className={`
          bg-white rounded-lg shadow p-6 transition-all duration-300
          ${selectedOrgId ? 'w-1/2 overflow-y-auto' : 'w-full max-w-4xl'}
        `}>
          <h3 className="text-lg font-semibold mb-4 text-gray-800">組織階層</h3>
          {orgTree && (
            <OrganizationTree
              nodes={[orgTree]}
              onSelectOrganization={handleSelectOrganization}
              selectedOrganizationId={selectedOrgId}
            />
          )}
        </div>

        {/* 編集パネル（グループ選択時のみ表示） */}
        {selectedOrgId && (
          <div className="w-1/2 bg-white rounded-lg shadow p-6 overflow-y-auto">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-semibold text-gray-800">組織の編集</h3>
              <button
                onClick={handleCloseEditPanel}
                className="px-3 py-1 text-sm text-gray-700 hover:text-gray-900 hover:bg-gray-100 rounded font-medium"
              >
                ✕ 閉じる
              </button>
            </div>

            {/* 権限情報 */}
            <div className="mb-6 p-4 bg-gray-50 rounded border border-gray-200">
              <h4 className="font-semibold mb-2 text-sm text-gray-800">あなたの権限</h4>
              <div className="space-y-1 text-xs text-gray-700">
                <p>役割: <span className="font-semibold text-gray-900">{editPanelPermissions.role || 'なし'}</span></p>
                <p className="flex items-center gap-1">
                  <span>メンバー追加:</span>
                  <span className={editPanelPermissions.canAddMember ? 'text-green-700 font-semibold' : 'text-gray-600'}>
                    {editPanelPermissions.canAddMember ? '✓ 可能' : '✗ 不可'}
                  </span>
                </p>
                <p className="flex items-center gap-1">
                  <span>メンバー削除:</span>
                  <span className={editPanelPermissions.canRemoveMember ? 'text-green-700 font-semibold' : 'text-gray-600'}>
                    {editPanelPermissions.canRemoveMember ? '✓ 可能' : '✗ 不可'}
                  </span>
                </p>
                <p className="flex items-center gap-1">
                  <span>子組織作成:</span>
                  <span className={editPanelPermissions.canCreateSubOrganization ? 'text-green-700 font-semibold' : 'text-gray-600'}>
                    {editPanelPermissions.canCreateSubOrganization ? '✓ 可能' : '✗ 不可'}
                  </span>
                </p>
              </div>
            </div>

            {/* メンバー一覧 */}
            <div className="mb-6">
              <div className="flex items-center justify-between mb-3">
                <h4 className="text-md font-semibold text-gray-800">メンバー管理</h4>
                {editPanelPermissions.canAddMember && (
                  <button
                    onClick={() => setShowAddMemberForm(!showAddMemberForm)}
                    className="px-3 py-1.5 text-sm bg-blue-600 text-white rounded hover:bg-blue-700 font-medium"
                  >
                    {showAddMemberForm ? 'キャンセル' : '+ メンバー追加'}
                  </button>
                )}
              </div>

              {showAddMemberForm && editPanelPermissions.canAddMember && (
                <form onSubmit={handleAddMember} className="p-4 bg-gray-50 border border-gray-200 rounded space-y-3 mb-4">
                  <div>
                    <label className="block text-sm font-semibold mb-1 text-gray-800">ユーザー名</label>
                    <input
                      type="text"
                      value={usernameToAdd}
                      onChange={(e) => setUsernameToAdd(e.target.value)}
                      className="w-full px-3 py-2 border border-gray-300 rounded text-sm text-gray-900 focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                      required
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-semibold mb-1 text-gray-800">役割</label>
                    <select
                      value={roleToAdd}
                      onChange={(e) => setRoleToAdd(e.target.value as OrganizationRole)}
                      className="w-full px-3 py-2 border border-gray-300 rounded text-sm text-gray-900 focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
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
                    className="w-full px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700 text-sm font-semibold"
                  >
                    追加
                  </button>
                </form>
              )}

              {/* メンバー一覧 */}
              <div className="space-y-3">
                <div className="flex items-center justify-between">
                  <h5 className="text-sm font-semibold text-gray-800">メンバー ({editPanelMembers.length}人)</h5>
                  <div className="flex gap-2">
                    <select
                      value={sortBy}
                      onChange={(e) => setSortBy(e.target.value as 'name' | 'role' | 'joinedAt')}
                      className="px-2 py-1 text-xs border border-gray-300 rounded text-gray-800 font-medium"
                    >
                      <option value="joinedAt">参加日時</option>
                      <option value="name">名前</option>
                      <option value="role">役割</option>
                    </select>
                    <button
                      onClick={() => setSortOrder(sortOrder === 'asc' ? 'desc' : 'asc')}
                      className="px-2 py-1 text-xs border border-gray-300 rounded hover:bg-gray-50 text-gray-700 font-medium"
                    >
                      {sortOrder === 'asc' ? '↑' : '↓'}
                    </button>
                  </div>
                </div>
                {editPanelMembers.length > 0 ? (
                  <div className="space-y-2 max-h-[300px] overflow-y-auto">
                    {editPanelMembers.map((member) => (
                      <div key={member.id} className="flex items-center justify-between p-3 bg-gray-50 border border-gray-200 rounded hover:bg-gray-100 transition-colors">
                        <div className="min-w-0 flex-1">
                          <p className="font-semibold text-sm truncate text-gray-900">{member.displayName || member.username}</p>
                          <p className="text-xs text-gray-600">@{member.username}</p>
                        </div>
                        <div className="ml-2">
                          <span className="px-2 py-1 text-xs font-medium rounded bg-blue-100 text-blue-900 whitespace-nowrap">
                            {member.role === 'ADMIN_ROOT' && '最高権限'}
                            {member.role === 'ADMIN_CORE' && 'コア管理者'}
                            {member.role === 'ADMIN_LEAD' && 'リード管理者'}
                            {member.role === 'ADMIN_SUPER' && 'スーパー管理者'}
                            {member.role === 'MEMBER' && '一般メンバー'}
                          </span>
                        </div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="text-sm text-gray-600 font-medium text-center py-4">メンバーがいません</p>
                )}
              </div>
            </div>

            {/* 子組織作成 */}
            {editPanelPermissions.canCreateSubOrganization && (
              <div className="mb-6">
                <div className="flex items-center justify-between mb-3">
                  <h4 className="text-md font-semibold text-gray-800">子組織作成</h4>
                  <button
                    onClick={() => setShowCreateSubOrgForm(!showCreateSubOrgForm)}
                    className="px-3 py-1.5 text-sm bg-purple-600 text-white rounded hover:bg-purple-700 font-medium"
                  >
                    {showCreateSubOrgForm ? 'キャンセル' : '+ 子組織作成'}
                  </button>
                </div>

                {showCreateSubOrgForm && (
                  <form onSubmit={handleCreateSubOrganization} className="p-4 bg-gray-50 border border-gray-200 rounded space-y-3">
                    <div>
                      <label className="block text-sm font-semibold mb-1 text-gray-800">組織名</label>
                      <input
                        type="text"
                        value={subOrgName}
                        onChange={(e) => setSubOrgName(e.target.value)}
                        className="w-full px-3 py-2 border border-gray-300 rounded text-sm text-gray-900 focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                        required
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-semibold mb-1 text-gray-800">説明</label>
                      <textarea
                        value={subOrgDescription}
                        onChange={(e) => setSubOrgDescription(e.target.value)}
                        className="w-full px-3 py-2 border border-gray-300 rounded text-sm text-gray-900 focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                        rows={3}
                      />
                    </div>
                    <button
                      type="submit"
                      className="w-full px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700 text-sm font-semibold"
                    >
                      作成
                    </button>
                  </form>
                )}
              </div>
            )}

            {/* 閲覧のみの場合のメッセージ */}
            {!editPanelPermissions.canAddMember && !editPanelPermissions.canRemoveMember && !editPanelPermissions.canCreateSubOrganization && (
              <div className={`p-4 border rounded ${editPanelMembers.length > 0 ? 'bg-blue-50 border-blue-300' : 'bg-amber-50 border-amber-300'}`}>
                <p className={`text-sm font-medium ${editPanelMembers.length > 0 ? 'text-blue-900' : 'text-amber-900'}`}>
                  {editPanelMembers.length > 0 ? (
                    <>📋 閲覧モード: この組織のメンバー情報を閲覧できますが、編集権限はありません。</>
                  ) : (
                    <>🔒 アクセス制限: この組織のメンバー情報を閲覧する権限がありません。</>
                  )}
                </p>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
};

export default OrganizationManagement;
