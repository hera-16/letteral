'use client';

import { useState, useEffect } from 'react';
import { groupService, authService, User } from '@/services/api';

interface GroupSettingsProps {
  groupId: number;
  onClose: () => void;
  onUpdate?: () => void;
}

interface GroupDetails {
  id: number;
  name: string;
  description?: string;
  groupType: 'INVITE_ONLY' | 'PUBLIC_TOPIC';
  creator?: {
    id: number;
    username: string;
    displayName?: string;
  };
}

export default function GroupSettings({ groupId, onClose, onUpdate }: GroupSettingsProps) {
  const [group, setGroup] = useState<GroupDetails | null>(null);
  const [members, setMembers] = useState<any[]>([]);
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [newMemberUsername, setNewMemberUsername] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [isAdmin, setIsAdmin] = useState(false);

  useEffect(() => {
    loadGroupData();
  }, [groupId]);

  const loadGroupData = async () => {
    try {
      setIsLoading(true);
      const [groupData, membersData] = await Promise.all([
        groupService.getGroup(groupId),
        groupService.getGroupMembers(groupId),
      ]);
      
      setGroup(groupData);
      setName(groupData.name);
      setDescription(groupData.description || '');
      setMembers(membersData);

      // 現在のユーザーがADMIN（=creator）かチェック
      const currentUser = authService.getCurrentUser();
      if (currentUser) {
        setIsAdmin(groupData.creator?.id === currentUser.id);
      }
    } catch (err) {
      setError('グループ情報の読み込みに失敗しました');
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  const handleUpdateGroup = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!isAdmin) {
      setError('管理者のみがグループを編集できます');
      return;
    }

    try {
      setIsLoading(true);
      setError('');
      await groupService.updateGroup(groupId, { name, description });
      setSuccess('グループ情報を更新しました');
      onUpdate?.();
      setTimeout(() => setSuccess(''), 3000);
    } catch (err: any) {
      setError(err.response?.data?.message || 'グループの更新に失敗しました');
    } finally {
      setIsLoading(false);
    }
  };

  const handleAddMember = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!isAdmin) {
      setError('管理者のみがメンバーを追加できます');
      return;
    }
    if (!newMemberUsername.trim()) {
      setError('ユーザー名を入力してください');
      return;
    }

    try {
      setIsLoading(true);
      setError('');
      const updatedMembers = await groupService.addGroupMember(groupId, newMemberUsername);
      setMembers(updatedMembers);
      setNewMemberUsername('');
      setSuccess('メンバーを追加しました');
      setTimeout(() => setSuccess(''), 3000);
    } catch (err: any) {
      setError(err.response?.data?.message || 'メンバーの追加に失敗しました');
    } finally {
      setIsLoading(false);
    }
  };

  const handleRemoveMember = async (userId: number) => {
    if (!isAdmin) {
      setError('管理者のみがメンバーを削除できます');
      return;
    }

    if (!confirm('本当にこのメンバーを削除しますか?')) {
      return;
    }

    try {
      setIsLoading(true);
      setError('');
      const updatedMembers = await groupService.removeGroupMember(groupId, userId);
      setMembers(updatedMembers);
      setSuccess('メンバーを削除しました');
      setTimeout(() => setSuccess(''), 3000);
    } catch (err: any) {
      setError(err.response?.data?.message || 'メンバーの削除に失敗しました');
    } finally {
      setIsLoading(false);
    }
  };

  if (isLoading && !group) {
    return (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div className="bg-white rounded-lg p-8">
          <p className="text-gray-700">読み込み中...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg max-w-2xl w-full max-h-[90vh] overflow-y-auto">
        <div className="sticky top-0 bg-white border-b px-6 py-4 flex justify-between items-center">
          <h2 className="text-2xl font-bold text-gray-800">グループ設定</h2>
          <button
            onClick={onClose}
            className="text-gray-500 hover:text-gray-700 text-2xl"
          >
            ×
          </button>
        </div>

        <div className="p-6 space-y-6">
          {/* エラー・成功メッセージ */}
          {error && (
            <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
              {error}
            </div>
          )}
          {success && (
            <div className="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded">
              {success}
            </div>
          )}

          {!isAdmin && (
            <div className="bg-yellow-100 border border-yellow-400 text-yellow-700 px-4 py-3 rounded">
              閲覧専用: 管理者のみが編集できます
            </div>
          )}

          {/* グループ情報編集 */}
          <form onSubmit={handleUpdateGroup} className="space-y-4">
            <h3 className="text-xl font-semibold text-gray-800">グループ情報</h3>
            
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                グループ名
              </label>
              <input
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                disabled={!isAdmin || isLoading}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:bg-gray-100"
                required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                説明
              </label>
              <textarea
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                disabled={!isAdmin || isLoading}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:bg-gray-100"
                rows={3}
              />
            </div>

            {isAdmin && (
              <button
                type="submit"
                disabled={isLoading}
                className="w-full bg-blue-500 text-white py-2 px-4 rounded-lg hover:bg-blue-600 disabled:opacity-50"
              >
                {isLoading ? '更新中...' : 'グループ情報を更新'}
              </button>
            )}
          </form>

          {/* メンバー管理 */}
          <div className="space-y-4">
            <h3 className="text-xl font-semibold text-gray-800">メンバー管理</h3>

            {/* メンバー追加 */}
            {isAdmin && (
              <form onSubmit={handleAddMember} className="flex gap-2">
                <input
                  type="text"
                  value={newMemberUsername}
                  onChange={(e) => setNewMemberUsername(e.target.value)}
                  placeholder="ユーザー名を入力"
                  disabled={isLoading}
                  className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
                <button
                  type="submit"
                  disabled={isLoading}
                  className="bg-green-500 text-white py-2 px-6 rounded-lg hover:bg-green-600 disabled:opacity-50"
                >
                  追加
                </button>
              </form>
            )}

            {/* メンバーリスト */}
            <div className="border border-gray-300 rounded-lg divide-y">
              {members.map((member: any) => {
                const isCreator = group?.creator?.id === member.id;
                return (
                  <div key={member.id} className="p-4 flex justify-between items-center">
                    <div>
                      <p className="font-medium text-gray-800">
                        {member.displayName || member.username || 'Unknown'}
                      </p>
                      <p className="text-sm text-gray-500">
                        {isCreator ? '管理者（作成者）' : 'メンバー'}
                      </p>
                    </div>
                    {isAdmin && !isCreator && (
                      <button
                        onClick={() => handleRemoveMember(member.id)}
                        disabled={isLoading}
                        className="text-red-500 hover:text-red-700 disabled:opacity-50"
                      >
                        削除
                      </button>
                    )}
                  </div>
                );
              })}
              {members.length === 0 && (
                <p className="p-4 text-gray-500 text-center">メンバーがいません</p>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
