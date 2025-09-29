'use client';

import React, { useState, useEffect, useCallback } from 'react';
import api from '@/services/api';

interface User {
  id: number;
  username: string;
  displayName: string;
  email: string;
}

interface Friend {
  id: number;
  requester: User;
  addressee: User;
  status: 'PENDING' | 'ACCEPTED' | 'REJECTED' | 'BLOCKED';
  requestedAt: string;
  respondedAt?: string;
}

interface FriendStats {
  friendCount: number;
  pendingRequestCount: number;
}

const FriendManagement: React.FC = () => {
  const [friends, setFriends] = useState<User[]>([]);
  const [pendingRequests, setPendingRequests] = useState<Friend[]>([]);
  const [sentRequests, setSentRequests] = useState<Friend[]>([]);
  const [stats, setStats] = useState<FriendStats>({ friendCount: 0, pendingRequestCount: 0 });
  const [targetUserId, setTargetUserId] = useState('');
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState<'friends' | 'requests' | 'sent'>('friends');

  const extractErrorMessage = (err: unknown): string | null => {
    if (typeof err === 'string') {
      return err;
    }

    if (err && typeof err === 'object') {
      if ('message' in err && typeof (err as { message: unknown }).message === 'string') {
        return (err as { message: string }).message;
      }

      const response = (err as { response?: { data?: unknown } }).response;
      if (response?.data) {
        if (typeof response.data === 'string') {
          return response.data;
        }

        if (
          typeof response.data === 'object' &&
          response.data !== null &&
          'message' in response.data &&
          typeof (response.data as { message: unknown }).message === 'string'
        ) {
          return (response.data as { message: string }).message;
        }
      }
    }

    return null;
  };

  const fetchFriends = useCallback(async () => {
    try {
      const response = await api.get<User[]>('/friends/list');
      setFriends(response.data);
    } catch (error) {
      console.error('フレンド一覧取得エラー:', error);
    }
  }, []);

  const fetchPendingRequests = useCallback(async () => {
    try {
      const response = await api.get<Friend[]>('/friends/requests/received');
      setPendingRequests(response.data);
    } catch (error) {
      console.error('フレンド申請取得エラー:', error);
    }
  }, []);

  const fetchSentRequests = useCallback(async () => {
    try {
      const response = await api.get<Friend[]>('/friends/requests/sent');
      setSentRequests(response.data);
    } catch (error) {
      console.error('送信済み申請取得エラー:', error);
    }
  }, []);

  const fetchStats = useCallback(async () => {
    try {
      const response = await api.get<FriendStats>('/friends/stats');
      setStats(response.data);
    } catch (error) {
      console.error('統計情報取得エラー:', error);
    }
  }, []);

  const fetchAllData = useCallback(async () => {
    try {
      await Promise.all([
        fetchFriends(),
        fetchPendingRequests(),
        fetchSentRequests(),
        fetchStats(),
      ]);
    } catch (error) {
      console.error('データ取得エラー:', error);
    } finally {
      setLoading(false);
    }
  }, [fetchFriends, fetchPendingRequests, fetchSentRequests, fetchStats]);

  useEffect(() => {
    void fetchAllData();
  }, [fetchAllData]);

  const sendFriendRequest = async () => {
    if (!targetUserId.trim()) return;

    try {
      await api.post('/friends/request', {
        targetUserId,
      });
      
      setTargetUserId('');
      void fetchSentRequests();
      void fetchStats();
      alert('フレンド申請を送信しました！');
    } catch (error) {
      console.error('フレンド申請エラー:', error);
      const message = extractErrorMessage(error) ?? 'フレンド申請の送信に失敗しました';
      alert(message);
    }
  };

  const acceptFriendRequest = async (requestId: number) => {
    try {
      await api.post(`/friends/accept/${requestId}`);

      void fetchAllData();
      alert('フレンド申請を承認しました！');
    } catch (error) {
      console.error('申請承認エラー:', error);
      alert('申請の承認に失敗しました');
    }
  };

  const rejectFriendRequest = async (requestId: number) => {
    try {
      await api.post(`/friends/reject/${requestId}`);

      void fetchPendingRequests();
      void fetchStats();
      alert('フレンド申請を拒否しました');
    } catch (error) {
      console.error('申請拒否エラー:', error);
      alert('申請の拒否に失敗しました');
    }
  };

  const removeFriend = async (friendId: number) => {
    if (!confirm('このフレンドを削除しますか？')) return;

    try {
      await api.delete(`/friends/${friendId}`);

      void fetchFriends();
      void fetchStats();
      alert('フレンドを削除しました');
    } catch (error) {
      console.error('フレンド削除エラー:', error);
      alert('フレンドの削除に失敗しました');
    }
  };

  if (loading) {
    return <div className="p-6">読み込み中...</div>;
  }

  return (
    <div className="p-6 max-w-4xl mx-auto">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold">フレンド管理</h1>
        <div className="flex space-x-4 text-sm">
          <span className="bg-blue-100 text-blue-800 px-3 py-1 rounded">
            フレンド: {stats.friendCount}名
          </span>
          <span className="bg-orange-100 text-orange-800 px-3 py-1 rounded">
            申請待ち: {stats.pendingRequestCount}件
          </span>
        </div>
      </div>

      {/* フレンド申請フォーム */}
      <div className="mb-6 p-4 bg-gray-50 rounded">
        <h3 className="font-medium mb-2">新しいフレンドを追加</h3>
        <div className="flex space-x-2">
          <input
            type="text"
            value={targetUserId}
            onChange={(e) => setTargetUserId(e.target.value)}
            placeholder="ユーザーIDを入力"
            className="border border-gray-300 rounded px-3 py-2 flex-1"
          />
          <button
            onClick={sendFriendRequest}
            className="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600"
          >
            申請送信
          </button>
        </div>
      </div>

      {/* タブナビゲーション */}
      <div className="border-b border-gray-200 mb-4">
        <nav className="-mb-px flex space-x-8">
          <button
            onClick={() => setActiveTab('friends')}
            className={`py-2 px-1 border-b-2 font-medium text-sm ${
              activeTab === 'friends'
                ? 'border-blue-500 text-blue-600'
                : 'border-transparent text-gray-500 hover:text-gray-700'
            }`}
          >
            フレンド ({stats.friendCount})
          </button>
          <button
            onClick={() => setActiveTab('requests')}
            className={`py-2 px-1 border-b-2 font-medium text-sm ${
              activeTab === 'requests'
                ? 'border-blue-500 text-blue-600'
                : 'border-transparent text-gray-500 hover:text-gray-700'
            }`}
          >
            受信した申請 ({stats.pendingRequestCount})
          </button>
          <button
            onClick={() => setActiveTab('sent')}
            className={`py-2 px-1 border-b-2 font-medium text-sm ${
              activeTab === 'sent'
                ? 'border-blue-500 text-blue-600'
                : 'border-transparent text-gray-500 hover:text-gray-700'
            }`}
          >
            送信した申請 ({sentRequests.length})
          </button>
        </nav>
      </div>

      {/* フレンド一覧 */}
      {activeTab === 'friends' && (
        <div className="space-y-4">
          <h2 className="text-lg font-semibold">フレンド一覧</h2>
          {friends.length === 0 ? (
            <p className="text-gray-500">まだフレンドがいません</p>
          ) : (
            friends.map((friend) => (
              <div key={friend.id} className="border border-gray-200 rounded p-4">
                <div className="flex justify-between items-center">
                  <div>
                    <h3 className="font-medium">{friend.displayName}</h3>
                    <p className="text-gray-600 text-sm">@{friend.username}</p>
                  </div>
                  <div className="flex space-x-2">
                    <button
                      onClick={() => window.location.href = `/chat/${friend.id}`}
                      className="bg-green-500 text-white px-3 py-1 rounded text-sm hover:bg-green-600"
                    >
                      チャット
                    </button>
                    <button
                      onClick={() => removeFriend(friend.id)}
                      className="bg-red-500 text-white px-3 py-1 rounded text-sm hover:bg-red-600"
                    >
                      削除
                    </button>
                  </div>
                </div>
              </div>
            ))
          )}
        </div>
      )}

      {/* 受信した申請 */}
      {activeTab === 'requests' && (
        <div className="space-y-4">
          <h2 className="text-lg font-semibold">受信したフレンド申請</h2>
          {pendingRequests.length === 0 ? (
            <p className="text-gray-500">新しい申請はありません</p>
          ) : (
            pendingRequests.map((request) => (
              <div key={request.id} className="border border-gray-200 rounded p-4">
                <div className="flex justify-between items-center">
                  <div>
                    <h3 className="font-medium">{request.requester.displayName}</h3>
                    <p className="text-gray-600 text-sm">@{request.requester.username}</p>
                    <p className="text-gray-500 text-xs">
                      {new Date(request.requestedAt).toLocaleString('ja-JP')}
                    </p>
                  </div>
                  <div className="flex space-x-2">
                    <button
                      onClick={() => acceptFriendRequest(request.id)}
                      className="bg-green-500 text-white px-3 py-1 rounded text-sm hover:bg-green-600"
                    >
                      承認
                    </button>
                    <button
                      onClick={() => rejectFriendRequest(request.id)}
                      className="bg-red-500 text-white px-3 py-1 rounded text-sm hover:bg-red-600"
                    >
                      拒否
                    </button>
                  </div>
                </div>
              </div>
            ))
          )}
        </div>
      )}

      {/* 送信した申請 */}
      {activeTab === 'sent' && (
        <div className="space-y-4">
          <h2 className="text-lg font-semibold">送信したフレンド申請</h2>
          {sentRequests.length === 0 ? (
            <p className="text-gray-500">送信中の申請はありません</p>
          ) : (
            sentRequests.map((request) => (
              <div key={request.id} className="border border-gray-200 rounded p-4">
                <div className="flex justify-between items-center">
                  <div>
                    <h3 className="font-medium">{request.addressee.displayName}</h3>
                    <p className="text-gray-600 text-sm">@{request.addressee.username}</p>
                    <p className="text-gray-500 text-xs">
                      {new Date(request.requestedAt).toLocaleString('ja-JP')}
                    </p>
                  </div>
                  <div className="text-right">
                    <span className="inline-block px-2 py-1 rounded text-xs bg-yellow-100 text-yellow-800">
                      申請中
                    </span>
                  </div>
                </div>
              </div>
            ))
          )}
        </div>
      )}
    </div>
  );
};

export default FriendManagement;