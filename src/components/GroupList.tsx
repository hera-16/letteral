'use client';

import { useState, useEffect } from 'react';
import { groupService, friendService, Group } from '@/services/api';

interface GroupListProps {
  onSelectGroup: (group: Group) => void;
}

export default function GroupList({ onSelectGroup }: GroupListProps) {
  const [myGroups, setMyGroups] = useState<Group[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // フレンドリストを読み込む
  const loadFriends = async () => {
    try {
      const data = await friendService.getFriends();
      setFriends(data);
    } catch (err) {
      console.error('Failed to load friends:', err);
    }
  };

  // グループ作成用の状態
  const [showCreateGroup, setShowCreateGroup] = useState(false);
  const [groupName, setGroupName] = useState('');
  const [groupDescription, setGroupDescription] = useState('');
  const [selectedMembers, setSelectedMembers] = useState<string[]>([]);
  const [friends, setFriends] = useState<any[]>([]);
  const [showAllFriends, setShowAllFriends] = useState(false);

  // 招待コード参加用の状態
  const [showJoinByCode, setShowJoinByCode] = useState(false);
  const [inviteCode, setInviteCode] = useState('');

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const data = await groupService.getMyInviteOnlyGroups();
      setMyGroups(data);
      setError(null);
    } catch (err: any) {
      setError(err.response?.data?.message || 'データの取得に失敗しました');
    } finally {
      setLoading(false);
    }
  };

  const handleCreateGroup = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const newGroup = await groupService.createInviteOnlyGroup({
        name: groupName,
        description: groupDescription,
      });
      
      // グループ作成後、選択されたメンバーを追加
      if (selectedMembers.length > 0 && newGroup.id) {
        for (const username of selectedMembers) {
          try {
            await groupService.addGroupMember(newGroup.id, username);
          } catch (err: any) {
            console.error(`Failed to add member ${username}:`, err);
            // メンバー追加エラーは無視して続行
          }
        }
      }
      
      setShowCreateGroup(false);
      setGroupName('');
      setGroupDescription('');
      setSelectedMembers([]);
      setShowAllFriends(false);
      loadData();
    } catch (err: any) {
      alert(err.response?.data?.message || 'グループの作成に失敗しました');
    }
  };

  const handleJoinByCode = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await groupService.joinGroupByInviteCode(inviteCode);
      setShowJoinByCode(false);
      setInviteCode('');
      loadData();
      alert('グループに参加しました!');
    } catch (err: any) {
      alert(err.response?.data?.message || 'グループへの参加に失敗しました');
    }
  };

  const toggleMemberSelection = (username: string) => {
    setSelectedMembers(prev =>
      prev.includes(username)
        ? prev.filter(u => u !== username)
        : [...prev, username]
    );
  };

  return (
    <div className="rounded-lg shadow p-4" style={{ backgroundColor: '#393E46' }}>
      <h2 className="text-2xl font-bold mb-4" style={{ color: '#EEEEEE' }}>匿名グループ</h2>

      {/* アクションボタン */}
      <div className="flex gap-2 mb-4">
        <button
          onClick={() => {
            setShowCreateGroup(true);
            loadFriends();
          }}
          className="px-4 py-2 rounded-lg transition-opacity"
          style={{ backgroundColor: '#00ADB5', color: '#EEEEEE' }}
          onMouseEnter={(e) => e.currentTarget.style.opacity = '0.8'}
          onMouseLeave={(e) => e.currentTarget.style.opacity = '1'}
        >
          グループ作成
        </button>
        <button
          onClick={() => setShowJoinByCode(true)}
          className="px-4 py-2 rounded-lg transition-opacity"
          style={{ backgroundColor: '#00ADB5', color: '#EEEEEE' }}
          onMouseEnter={(e) => e.currentTarget.style.opacity = '0.8'}
          onMouseLeave={(e) => e.currentTarget.style.opacity = '1'}
        >
          招待コードで参加
        </button>
      </div>

      {/* エラー表示 */}
      {error && (
        <div className="mb-4 p-3 border rounded" style={{ backgroundColor: '#222831', borderColor: '#00ADB5', color: '#EEEEEE' }}>
          {error}
        </div>
      )}

      {/* ローディング */}
      {loading && (
        <div className="text-center py-8">
          <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500"></div>
        </div>
      )}

      {/* マイグループ一覧 */}
      {!loading && (
        <div className="space-y-2">
          {myGroups.length === 0 ? (
            <p className="text-center py-4" style={{ color: '#EEEEEE' }}>参加しているグループがありません</p>
          ) : (
            myGroups.map((group) => (
              <div
                key={group.id}
                className="p-4 border rounded-lg cursor-pointer transition-opacity"
                style={{ backgroundColor: '#222831', borderColor: '#00ADB5' }}
                onClick={() => onSelectGroup(group)}
                onMouseEnter={(e) => e.currentTarget.style.opacity = '0.8'}
                onMouseLeave={(e) => e.currentTarget.style.opacity = '1'}
              >
                <div className="flex justify-between items-start">
                  <div>
                    <h3 className="font-semibold" style={{ color: '#EEEEEE' }}>{group.name}</h3>
                    {group.description && (
                      <p className="text-sm mt-1" style={{ color: '#EEEEEE' }}>{group.description}</p>
                    )}
                    <p className="text-xs mt-2" style={{ color: '#00ADB5' }}>
                      招待コード: <span className="font-mono px-2 py-1 rounded" style={{ backgroundColor: '#393E46' }}>{group.inviteCode}</span>
                    </p>
                  </div>
                </div>
              </div>
            ))
          )}
        </div>
      )}

      {/* グループ作成モーダル */}
      {showCreateGroup && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="rounded-lg p-6 max-w-md w-full mx-4 max-h-[90vh] overflow-y-auto" style={{ backgroundColor: '#393E46' }}>
            <h3 className="text-xl font-bold mb-4" style={{ color: '#EEEEEE' }}>新しいグループを作成</h3>
            <form onSubmit={handleCreateGroup}>
              <div className="mb-4">
                <label className="block text-sm font-medium mb-2" style={{ color: '#EEEEEE' }}>グループ名 *</label>
                <input
                  type="text"
                  value={groupName}
                  onChange={(e) => setGroupName(e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg"
                  style={{ backgroundColor: '#222831', borderColor: '#00ADB5', color: '#EEEEEE' }}
                  required
                />
              </div>
              <div className="mb-4">
                <label className="block text-sm font-medium mb-2" style={{ color: '#EEEEEE' }}>説明</label>
                <textarea
                  value={groupDescription}
                  onChange={(e) => setGroupDescription(e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg"
                  style={{ backgroundColor: '#222831', borderColor: '#00ADB5', color: '#EEEEEE' }}
                  rows={3}
                />
              </div>

              {/* フレンド選択 */}
              <div className="mb-4">
                <label className="block text-sm font-medium mb-2" style={{ color: '#EEEEEE' }}>メンバーを追加（任意）</label>
                <div className="border rounded-lg p-3 max-h-48 overflow-y-auto" style={{ backgroundColor: '#222831', borderColor: '#00ADB5' }}>
                  {friends.length === 0 ? (
                    <p className="text-sm" style={{ color: '#EEEEEE' }}>フレンドがいません</p>
                  ) : (
                    <>
                      {friends.slice(0, showAllFriends ? friends.length : 5).map((friend) => (
                        <div key={friend.id} className="flex items-center mb-2">
                          <input
                            type="checkbox"
                            id={`friend-${friend.id}`}
                            checked={selectedMembers.includes(friend.username)}
                            onChange={() => toggleMemberSelection(friend.username)}
                            className="mr-2"
                          />
                          <label htmlFor={`friend-${friend.id}`} className="text-sm cursor-pointer" style={{ color: '#EEEEEE' }}>
                            {friend.displayName || friend.username}
                          </label>
                        </div>
                      ))}
                      {friends.length > 5 && (
                        <button
                          type="button"
                          onClick={() => setShowAllFriends(!showAllFriends)}
                          className="text-sm hover:underline mt-2"
                          style={{ color: '#00ADB5' }}
                        >
                          {showAllFriends ? '閉じる' : `他 ${friends.length - 5} 人を表示`}
                        </button>
                      )}
                    </>
                  )}
                </div>
                {selectedMembers.length > 0 && (
                  <p className="text-xs mt-1" style={{ color: '#EEEEEE' }}>
                    {selectedMembers.length} 人を選択中
                  </p>
                )}
              </div>

              <div className="flex gap-2">
                <button
                  type="submit"
                  className="flex-1 px-4 py-2 rounded-lg transition-opacity"
                  style={{ backgroundColor: '#00ADB5', color: '#EEEEEE' }}
                  onMouseEnter={(e) => e.currentTarget.style.opacity = '0.8'}
                  onMouseLeave={(e) => e.currentTarget.style.opacity = '1'}
                >
                  作成
                </button>
                <button
                  type="button"
                  onClick={() => {
                    setShowCreateGroup(false);
                    setGroupName('');
                    setGroupDescription('');
                    setSelectedMembers([]);
                    setShowAllFriends(false);
                  }}
                  className="flex-1 px-4 py-2 rounded-lg transition-opacity"
                  style={{ backgroundColor: '#222831', color: '#EEEEEE' }}
                  onMouseEnter={(e) => e.currentTarget.style.opacity = '0.8'}
                  onMouseLeave={(e) => e.currentTarget.style.opacity = '1'}
                >
                  キャンセル
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* 招待コード参加モーダル */}
      {showJoinByCode && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="rounded-lg p-6 max-w-md w-full mx-4" style={{ backgroundColor: '#393E46' }}>
            <h3 className="text-xl font-bold mb-4" style={{ color: '#EEEEEE' }}>招待コードで参加</h3>
            <form onSubmit={handleJoinByCode}>
              <div className="mb-4">
                <label className="block text-sm font-medium mb-2" style={{ color: '#EEEEEE' }}>招待コード</label>
                <input
                  type="text"
                  value={inviteCode}
                  onChange={(e) => setInviteCode(e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg font-mono"
                  style={{ backgroundColor: '#222831', borderColor: '#00ADB5', color: '#EEEEEE' }}
                  placeholder="ABC123"
                  required
                />
              </div>
              <div className="flex gap-2">
                <button
                  type="submit"
                  className="flex-1 px-4 py-2 rounded-lg transition-opacity"
                  style={{ backgroundColor: '#00ADB5', color: '#EEEEEE' }}
                  onMouseEnter={(e) => e.currentTarget.style.opacity = '0.8'}
                  onMouseLeave={(e) => e.currentTarget.style.opacity = '1'}
                >
                  参加
                </button>
                <button
                  type="button"
                  onClick={() => {
                    setShowJoinByCode(false);
                    setInviteCode('');
                  }}
                  className="flex-1 px-4 py-2 rounded-lg transition-opacity"
                  style={{ backgroundColor: '#222831', color: '#EEEEEE' }}
                  onMouseEnter={(e) => e.currentTarget.style.opacity = '0.8'}
                  onMouseLeave={(e) => e.currentTarget.style.opacity = '1'}
                >
                  キャンセル
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
