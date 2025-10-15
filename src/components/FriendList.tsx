'use client';

import { useState, useEffect } from 'react';
import { friendService, User, Friend, FriendWithId } from '@/services/api';

interface FriendListProps {
  onSelectFriend: (friend: User, friendshipId: number) => void;
}

export default function FriendList({ onSelectFriend }: FriendListProps) {
  const [friends, setFriends] = useState<FriendWithId[]>([]);
  const [pendingRequests, setPendingRequests] = useState<Friend[]>([]);
  const [sentRequests, setSentRequests] = useState<Friend[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<'friends' | 'pending' | 'sent'>('friends');
  const [searchUsername, setSearchUsername] = useState('');
  const [searchError, setSearchError] = useState<string | null>(null);
  const [searchSuccess, setSearchSuccess] = useState<string | null>(null);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      
      // トークンの確認
      const token = localStorage.getItem('token');
      console.log('Loading friend data, token exists:', !!token);
      
      // 一時的に古いエンドポイントも試す
      let friendsData: FriendWithId[];
      try {
        console.log('Trying new endpoint: /friends/list/detailed');
        friendsData = await friendService.getFriendsWithIds();
        console.log('New endpoint succeeded:', friendsData);
      } catch (detailedError: any) {
        console.warn('New endpoint failed, falling back to old endpoint:', detailedError);
        // フォールバック: 古いエンドポイントを使用
        const oldFriendsData = await friendService.getFriends();
        console.log('Old endpoint data:', oldFriendsData);
        // User[]をFriendWithId[]に変換（friendshipIdはユーザーIDで代用）
        friendsData = oldFriendsData.map(user => ({
          friendshipId: user.id, // 一時的にuser.idを使用
          userId: user.id,
          username: user.username,
          displayName: user.displayName || '',
          email: user.email || ''
        }));
      }
      
      const [pendingData, sentData] = await Promise.all([
        friendService.getPendingRequests(),
        friendService.getSentRequests(),
      ]);
      setFriends(Array.isArray(friendsData) ? friendsData : []);
      setPendingRequests(Array.isArray(pendingData) ? pendingData : []);
      setSentRequests(Array.isArray(sentData) ? sentData : []);
      setError(null);
    } catch (err: any) {
      console.error('Friend data loading error:', err);
      console.error('Error response:', err.response);
      console.error('Error status:', err.response?.status);
      console.error('Token in localStorage:', localStorage.getItem('token'));
      
      setFriends([]);
      setPendingRequests([]);
      setSentRequests([]);
      
      if (err.response?.status === 401) {
        setError('認証エラー: ログインし直してください');
      } else {
        setError(err.response?.data?.message || 'フレンド情報の取得に失敗しました');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleSendRequest = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!searchUsername.trim()) return;

    try {
      setSearchError(null);
      await friendService.sendFriendRequest(searchUsername);
      setSearchSuccess(`${searchUsername} にフレンドリクエストを送信しました`);
      setSearchUsername('');
      setTimeout(() => setSearchSuccess(null), 3000);
      loadData();
    } catch (err: any) {
      setSearchError(err.response?.data?.message || 'フレンドリクエストの送信に失敗しました');
    }
  };

  const handleAcceptRequest = async (friendshipId: number) => {
    try {
      await friendService.acceptFriendRequest(friendshipId);
      loadData();
    } catch (err: any) {
      alert(err.response?.data?.message || 'リクエストの承認に失敗しました');
    }
  };

  const handleRejectRequest = async (friendshipId: number) => {
    try {
      await friendService.rejectFriendRequest(friendshipId);
      loadData();
    } catch (err: any) {
      alert(err.response?.data?.message || 'リクエストの拒否に失敗しました');
    }
  };

  const handleRemoveFriend = async (friendId: number) => {
    if (!confirm('このフレンドを削除しますか?')) return;

    try {
      await friendService.removeFriend(friendId);
      loadData();
    } catch (err: any) {
      alert(err.response?.data?.message || 'フレンドの削除に失敗しました');
    }
  };

  if (loading) {
    return <div className="text-center py-4">読み込み中...</div>;
  }

  return (
    <div className="rounded-lg shadow p-4" style={{ backgroundColor: '#393E46' }}>
      <h2 className="text-2xl font-bold mb-4" style={{ color: '#EEEEEE' }}>フレンド管理</h2>

      {/* フレンド検索 */}
      <form onSubmit={handleSendRequest} className="mb-6">
        <div className="flex gap-2">
          <input
            type="text"
            value={searchUsername}
            onChange={(e) => setSearchUsername(e.target.value)}
            placeholder="ユーザー名で検索"
            className="flex-1 px-4 py-2 border rounded-lg focus:outline-none focus:ring-2"
            style={{ backgroundColor: '#222831', borderColor: '#00ADB5', color: '#EEEEEE' }}
          />
          <button
            type="submit"
            className="px-4 py-2 rounded-lg transition-opacity"
            style={{ backgroundColor: '#00ADB5', color: '#EEEEEE' }}
            onMouseEnter={(e) => e.currentTarget.style.opacity = '0.8'}
            onMouseLeave={(e) => e.currentTarget.style.opacity = '1'}
          >
            リクエスト送信
          </button>
        </div>
        {searchError && (
          <p className="mt-2 text-sm" style={{ color: '#EEEEEE' }}>{searchError}</p>
        )}
        {searchSuccess && (
          <p className="mt-2 text-sm" style={{ color: '#00ADB5' }}>{searchSuccess}</p>
        )}
      </form>

      {/* タブ */}
      <div className="flex gap-4 mb-4 border-b" style={{ borderColor: '#00ADB5' }}>
        <button
          onClick={() => setActiveTab('friends')}
          className={`px-4 py-2 ${
            activeTab === 'friends'
              ? 'border-b-2 font-semibold'
              : ''
          }`}
          style={{
            borderColor: activeTab === 'friends' ? '#00ADB5' : 'transparent',
            color: activeTab === 'friends' ? '#00ADB5' : '#EEEEEE'
          }}
        >
          フレンド ({friends.length})
        </button>
        <button
          onClick={() => setActiveTab('pending')}
          className={`px-4 py-2 ${
            activeTab === 'pending'
              ? 'border-b-2 font-semibold'
              : ''
          }`}
          style={{
            borderColor: activeTab === 'pending' ? '#00ADB5' : 'transparent',
            color: activeTab === 'pending' ? '#00ADB5' : '#EEEEEE'
          }}
        >
          リクエスト ({pendingRequests.length})
        </button>
        <button
          onClick={() => setActiveTab('sent')}
          className={`px-4 py-2 ${
            activeTab === 'sent'
              ? 'border-b-2 font-semibold'
              : ''
          }`}
          style={{
            borderColor: activeTab === 'sent' ? '#00ADB5' : 'transparent',
            color: activeTab === 'sent' ? '#00ADB5' : '#EEEEEE'
          }}
        >
          送信済み ({sentRequests.length})
        </button>
      </div>

      {error && (
        <div className="mb-4 p-3 rounded" style={{ backgroundColor: '#222831', color: '#EEEEEE' }}>
          {error}
        </div>
      )}

      {/* フレンド一覧 */}
      {activeTab === 'friends' && (
        <div className="space-y-2">
          {!Array.isArray(friends) || friends.length === 0 ? (
            <p className="text-center py-4" style={{ color: '#EEEEEE' }}>
              フレンドがいません
            </p>
          ) : (
            friends.map((friend) => (
              <div
                key={friend.friendshipId}
                className="flex items-center justify-between p-3 border rounded-lg transition-opacity"
                style={{ backgroundColor: '#222831', borderColor: '#00ADB5' }}
                onMouseEnter={(e) => e.currentTarget.style.opacity = '0.8'}
                onMouseLeave={(e) => e.currentTarget.style.opacity = '1'}
              >
                <div
                  className="flex-1 cursor-pointer"
                  onClick={() => onSelectFriend({ id: friend.userId, username: friend.username, displayName: friend.displayName, email: friend.email } as User, friend.friendshipId)}
                >
                  <p className="font-semibold" style={{ color: '#EEEEEE' }}>{friend.displayName || friend.username}</p>
                  <p className="text-sm" style={{ color: '#00ADB5' }}>@{friend.username}</p>
                </div>
                <div className="flex gap-2">
                  <button
                    onClick={() => onSelectFriend({ id: friend.userId, username: friend.username, displayName: friend.displayName, email: friend.email } as User, friend.friendshipId)}
                    className="px-3 py-1 rounded transition-opacity"
                    style={{ backgroundColor: '#00ADB5', color: '#EEEEEE' }}
                    onMouseEnter={(e) => e.currentTarget.style.opacity = '0.8'}
                    onMouseLeave={(e) => e.currentTarget.style.opacity = '1'}
                  >
                    チャット
                  </button>
                  <button
                    onClick={() => handleRemoveFriend(friend.friendshipId)}
                    className="px-3 py-1 rounded transition-opacity"
                    style={{ backgroundColor: '#393E46', color: '#EEEEEE' }}
                    onMouseEnter={(e) => e.currentTarget.style.opacity = '0.8'}
                    onMouseLeave={(e) => e.currentTarget.style.opacity = '1'}
                  >
                    削除
                  </button>
                </div>
              </div>
            ))
          )}
        </div>
      )}

      {/* 受信リクエスト */}
      {activeTab === 'pending' && (
        <div className="space-y-2">
          {pendingRequests.length === 0 ? (
            <p className="text-center py-4" style={{ color: '#EEEEEE' }}>
              保留中のリクエストはありません
            </p>
          ) : (
            pendingRequests.map((request) => (
              <div
                key={request.id}
                className="flex items-center justify-between p-3 border rounded-lg"
                style={{ backgroundColor: '#222831', borderColor: '#00ADB5' }}
              >
                <div>
                  <p className="font-semibold" style={{ color: '#EEEEEE' }}>
                    {request.requester.displayName || request.requester.username}
                  </p>
                  <p className="text-sm" style={{ color: '#00ADB5' }}>@{request.requester.username}</p>
                  <p className="text-xs" style={{ color: '#EEEEEE' }}>
                    {new Date(request.requestedAt).toLocaleString()}
                  </p>
                </div>
                <div className="flex gap-2">
                  <button
                    onClick={() => handleAcceptRequest(request.id)}
                    className="px-3 py-1 rounded transition-opacity"
                    style={{ backgroundColor: '#00ADB5', color: '#EEEEEE' }}
                    onMouseEnter={(e) => e.currentTarget.style.opacity = '0.8'}
                    onMouseLeave={(e) => e.currentTarget.style.opacity = '1'}
                  >
                    承認
                  </button>
                  <button
                    onClick={() => handleRejectRequest(request.id)}
                    className="px-3 py-1 rounded transition-opacity"
                    style={{ backgroundColor: '#393E46', color: '#EEEEEE' }}
                    onMouseEnter={(e) => e.currentTarget.style.opacity = '0.8'}
                    onMouseLeave={(e) => e.currentTarget.style.opacity = '1'}
                  >
                    拒否
                  </button>
                </div>
              </div>
            ))
          )}
        </div>
      )}

      {/* 送信済みリクエスト */}
      {activeTab === 'sent' && (
        <div className="space-y-2">
          {sentRequests.length === 0 ? (
            <p className="text-center py-4" style={{ color: '#EEEEEE' }}>
              送信済みのリクエストはありません
            </p>
          ) : (
            sentRequests.map((request) => (
              <div
                key={request.id}
                className="flex items-center justify-between p-3 border rounded-lg"
                style={{ backgroundColor: '#222831', borderColor: '#00ADB5' }}
              >
                <div>
                  <p className="font-semibold" style={{ color: '#EEEEEE' }}>
                    {request.addressee.displayName || request.addressee.username}
                  </p>
                  <p className="text-sm" style={{ color: '#00ADB5' }}>@{request.addressee.username}</p>
                  <p className="text-xs" style={{ color: '#EEEEEE' }}>
                    {new Date(request.requestedAt).toLocaleString()}
                  </p>
                </div>
                <span className="px-3 py-1 rounded text-sm" style={{ backgroundColor: '#00ADB5', color: '#EEEEEE' }}>
                  保留中
                </span>
              </div>
            ))
          )}
        </div>
      )}
    </div>
  );
}
