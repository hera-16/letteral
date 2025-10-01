'use client';

import { useState, useEffect } from 'react';
import { groupService, topicService, friendService, Group, Topic } from '@/services/api';

interface GroupTopicListProps {
  onSelectGroup: (group: Group) => void;
  onSelectTopic: (topic: Topic) => void;
}

export default function GroupTopicList({ onSelectGroup, onSelectTopic }: GroupTopicListProps) {
  const [activeTab, setActiveTab] = useState<'myGroups' | 'publicTopics' | 'allTopics'>('myGroups');
  const [myGroups, setMyGroups] = useState<Group[]>([]);
  const [publicTopics, setPublicTopics] = useState<Group[]>([]);
  const [allTopics, setAllTopics] = useState<Topic[]>([]);
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

  // トピック作成用の状態
  const [showCreateTopic, setShowCreateTopic] = useState(false);
  const [topicName, setTopicName] = useState('');
  const [topicDescription, setTopicDescription] = useState('');
  const [topicCategory, setTopicCategory] = useState('');

  // 招待コード参加用の状態
  const [showJoinByCode, setShowJoinByCode] = useState(false);
  const [inviteCode, setInviteCode] = useState('');

  useEffect(() => {
    loadData();
  }, [activeTab]);

  const loadData = async () => {
    try {
      setLoading(true);
      if (activeTab === 'myGroups') {
        const data = await groupService.getMyInviteOnlyGroups();
        setMyGroups(data);
      } else if (activeTab === 'publicTopics') {
        const data = await groupService.getPublicTopics();
        setPublicTopics(data);
      } else if (activeTab === 'allTopics') {
        const data = await topicService.getAllTopics();
        setAllTopics(data);
      }
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

  const handleCreateTopic = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await topicService.createTopic({
        name: topicName,
        description: topicDescription,
        category: topicCategory,
      });
      setShowCreateTopic(false);
      setTopicName('');
      setTopicDescription('');
      setTopicCategory('');
      loadData();
    } catch (err: any) {
      alert(err.response?.data?.message || 'トピックの作成に失敗しました');
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

  const handleJoinPublicTopic = async (groupId: number) => {
    try {
      await groupService.joinPublicTopic(groupId);
      alert('パブリックトピックに参加しました!');
    } catch (err: any) {
      alert(err.response?.data?.message || 'トピックへの参加に失敗しました');
    }
  };

  return (
    <div className="bg-white rounded-lg shadow p-4">
      <h2 className="text-2xl font-bold mb-4">グループ & トピック</h2>

      {/* タブ */}
      <div className="flex gap-4 mb-4 border-b">
        <button
          onClick={() => setActiveTab('myGroups')}
          className={`px-4 py-2 ${
            activeTab === 'myGroups'
              ? 'border-b-2 border-blue-500 text-blue-500 font-semibold'
              : 'text-gray-600'
          }`}
        >
          マイグループ
        </button>
        <button
          onClick={() => setActiveTab('publicTopics')}
          className={`px-4 py-2 ${
            activeTab === 'publicTopics'
              ? 'border-b-2 border-blue-500 text-blue-500 font-semibold'
              : 'text-gray-600'
          }`}
        >
          パブリックトピック
        </button>
        <button
          onClick={() => setActiveTab('allTopics')}
          className={`px-4 py-2 ${
            activeTab === 'allTopics'
              ? 'border-b-2 border-blue-500 text-blue-500 font-semibold'
              : 'text-gray-600'
          }`}
        >
          全トピック
        </button>
      </div>

      {/* アクションボタン */}
      <div className="flex gap-2 mb-4">
        {activeTab === 'myGroups' && (
          <>
            <button
              onClick={() => {
                setShowCreateGroup(true);
                loadFriends();
              }}
              className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600"
            >
              グループ作成
            </button>
            <button
              onClick={() => setShowJoinByCode(true)}
              className="px-4 py-2 bg-green-500 text-white rounded-lg hover:bg-green-600"
            >
              招待コードで参加
            </button>
          </>
        )}
        {activeTab === 'allTopics' && (
          <button
            onClick={() => setShowCreateTopic(true)}
            className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600"
          >
            トピック作成
          </button>
        )}
      </div>

      {loading && <div className="text-center py-4">読み込み中...</div>}
      {error && <div className="mb-4 p-3 bg-red-100 text-red-700 rounded">{error}</div>}

      {/* マイグループ一覧 */}
      {!loading && activeTab === 'myGroups' && (
        <div className="space-y-2">
          {myGroups.length === 0 ? (
            <p className="text-gray-500 text-center py-4">グループがありません</p>
          ) : (
            myGroups.map((group) => (
              <div
                key={group.id}
                className="p-4 border rounded-lg hover:bg-gray-50 cursor-pointer"
                onClick={() => onSelectGroup(group)}
              >
                <h3 className="font-semibold">{group.name}</h3>
                {group.description && (
                  <p className="text-sm text-gray-600 mt-1">{group.description}</p>
                )}
                <div className="flex gap-4 mt-2 text-xs text-gray-500">
                  <span>招待制</span>
                  {group.inviteCode && <span className="font-mono">コード: {group.inviteCode}</span>}
                </div>
              </div>
            ))
          )}
        </div>
      )}

      {/* パブリックトピック一覧 */}
      {!loading && activeTab === 'publicTopics' && (
        <div className="space-y-2">
          {publicTopics.length === 0 ? (
            <p className="text-gray-500 text-center py-4">パブリックトピックがありません</p>
          ) : (
            publicTopics.map((group) => (
              <div key={group.id} className="p-4 border rounded-lg">
                <div className="flex justify-between items-start">
                  <div className="flex-1 cursor-pointer" onClick={() => onSelectGroup(group)}>
                    <h3 className="font-semibold">{group.name}</h3>
                    {group.description && (
                      <p className="text-sm text-gray-600 mt-1">{group.description}</p>
                    )}
                  </div>
                  <button
                    onClick={() => handleJoinPublicTopic(group.id)}
                    className="ml-4 px-3 py-1 bg-blue-500 text-white rounded hover:bg-blue-600"
                  >
                    参加
                  </button>
                </div>
              </div>
            ))
          )}
        </div>
      )}

      {/* 全トピック一覧 */}
      {!loading && activeTab === 'allTopics' && (
        <div className="space-y-2">
          {allTopics.length === 0 ? (
            <p className="text-gray-500 text-center py-4">トピックがありません</p>
          ) : (
            allTopics.map((topic) => (
              <div
                key={topic.id}
                className="p-4 border rounded-lg hover:bg-gray-50 cursor-pointer"
                onClick={() => onSelectTopic(topic)}
              >
                <div className="flex justify-between items-start">
                  <div>
                    <h3 className="font-semibold">{topic.name}</h3>
                    {topic.description && (
                      <p className="text-sm text-gray-600 mt-1">{topic.description}</p>
                    )}
                    <div className="flex gap-4 mt-2 text-xs text-gray-500">
                      <span className="px-2 py-1 bg-blue-100 text-blue-800 rounded">
                        {topic.category}
                      </span>
                      <span>{topic.isActive ? '✓ アクティブ' : '✗ 非アクティブ'}</span>
                    </div>
                  </div>
                </div>
              </div>
            ))
          )}
        </div>
      )}

      {/* グループ作成モーダル */}
      {showCreateGroup && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg p-6 w-full max-w-lg max-h-[90vh] overflow-y-auto">
            <h3 className="text-xl font-bold mb-4">招待制グループを作成</h3>
            <form onSubmit={handleCreateGroup}>
              <div className="mb-4">
                <label className="block text-sm font-semibold mb-2">グループ名</label>
                <input
                  type="text"
                  value={groupName}
                  onChange={(e) => setGroupName(e.target.value)}
                  required
                  className="w-full px-3 py-2 border rounded-lg"
                />
              </div>
              <div className="mb-4">
                <label className="block text-sm font-semibold mb-2">説明</label>
                <textarea
                  value={groupDescription}
                  onChange={(e) => setGroupDescription(e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg"
                  rows={3}
                />
              </div>
              <div className="mb-4">
                <label className="block text-sm font-semibold mb-2">
                  フレンドを招待（オプション）
                </label>
                {friends.length === 0 ? (
                  <p className="text-sm text-gray-500">フレンドがいません</p>
                ) : (
                  <>
                    <div className="border rounded-lg p-2 max-h-48 overflow-y-auto space-y-2">
                      {(showAllFriends ? friends : friends.slice(0, 3)).map((friend: any) => (
                        <label
                          key={friend.id}
                          className="flex items-center gap-2 p-2 hover:bg-gray-50 rounded cursor-pointer"
                        >
                          <input
                            type="checkbox"
                            checked={selectedMembers.includes(friend.username)}
                            onChange={(e) => {
                              if (e.target.checked) {
                                setSelectedMembers([...selectedMembers, friend.username]);
                              } else {
                                setSelectedMembers(selectedMembers.filter(u => u !== friend.username));
                              }
                            }}
                            className="w-4 h-4"
                          />
                          <span className="text-sm">
                            {friend.displayName || friend.username}
                          </span>
                        </label>
                      ))}
                    </div>
                    {friends.length > 3 && !showAllFriends && (
                      <button
                        type="button"
                        onClick={() => {
                          setShowAllFriends(true);
                          if (friends.length === 0) loadFriends();
                        }}
                        className="mt-2 text-sm text-blue-500 hover:text-blue-700"
                      >
                        もっと見る（残り{friends.length - 3}人）
                      </button>
                    )}
                    {showAllFriends && (
                      <button
                        type="button"
                        onClick={() => setShowAllFriends(false)}
                        className="mt-2 text-sm text-blue-500 hover:text-blue-700"
                      >
                        閉じる
                      </button>
                    )}
                  </>
                )}
                <p className="text-xs text-gray-500 mt-2">
                  {selectedMembers.length > 0 
                    ? `${selectedMembers.length}人を招待` 
                    : 'メンバーは後からでも追加できます'}
                </p>
              </div>
              <div className="flex gap-2">
                <button
                  type="submit"
                  className="flex-1 px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600"
                >
                  作成
                </button>
                <button
                  type="button"
                  onClick={() => {
                    setShowCreateGroup(false);
                    setSelectedMembers([]);
                    setShowAllFriends(false);
                  }}
                  className="flex-1 px-4 py-2 bg-gray-500 text-white rounded-lg hover:bg-gray-600"
                >
                  キャンセル
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* トピック作成モーダル */}
      {showCreateTopic && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 w-96">
            <h3 className="text-xl font-bold mb-4">トピックを作成</h3>
            <form onSubmit={handleCreateTopic}>
              <div className="mb-4">
                <label className="block text-sm font-semibold mb-2">トピック名</label>
                <input
                  type="text"
                  value={topicName}
                  onChange={(e) => setTopicName(e.target.value)}
                  required
                  className="w-full px-3 py-2 border rounded-lg"
                />
              </div>
              <div className="mb-4">
                <label className="block text-sm font-semibold mb-2">説明</label>
                <textarea
                  value={topicDescription}
                  onChange={(e) => setTopicDescription(e.target.value)}
                  className="w-full px-3 py-2 border rounded-lg"
                  rows={3}
                />
              </div>
              <div className="mb-4">
                <label className="block text-sm font-semibold mb-2">カテゴリー</label>
                <input
                  type="text"
                  value={topicCategory}
                  onChange={(e) => setTopicCategory(e.target.value)}
                  required
                  placeholder="例: プログラミング, 趣味, エンタメ"
                  className="w-full px-3 py-2 border rounded-lg"
                />
              </div>
              <div className="flex gap-2">
                <button
                  type="submit"
                  className="flex-1 px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600"
                >
                  作成
                </button>
                <button
                  type="button"
                  onClick={() => setShowCreateTopic(false)}
                  className="flex-1 px-4 py-2 bg-gray-500 text-white rounded-lg hover:bg-gray-600"
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
          <div className="bg-white rounded-lg p-6 w-96">
            <h3 className="text-xl font-bold mb-4">招待コードで参加</h3>
            <form onSubmit={handleJoinByCode}>
              <div className="mb-4">
                <label className="block text-sm font-semibold mb-2">招待コード</label>
                <input
                  type="text"
                  value={inviteCode}
                  onChange={(e) => setInviteCode(e.target.value)}
                  required
                  placeholder="8文字の招待コードを入力"
                  className="w-full px-3 py-2 border rounded-lg font-mono"
                  maxLength={8}
                />
              </div>
              <div className="flex gap-2">
                <button
                  type="submit"
                  className="flex-1 px-4 py-2 bg-green-500 text-white rounded-lg hover:bg-green-600"
                >
                  参加
                </button>
                <button
                  type="button"
                  onClick={() => setShowJoinByCode(false)}
                  className="flex-1 px-4 py-2 bg-gray-500 text-white rounded-lg hover:bg-gray-600"
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
