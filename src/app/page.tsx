'use client';

import { useState, useEffect } from 'react';
import LoginForm from '@/components/LoginForm';
import SignupForm from '@/components/SignupForm';
import FriendList from '@/components/FriendList';
import GroupTopicList from '@/components/GroupTopicList';
import ChatRoom from '@/components/ChatRoom';
import { authService, User, Group, Topic } from '@/services/api';

type AuthMode = 'login' | 'signup';
type ViewMode = 'friends' | 'groups' | 'chat';

interface ChatTarget {
  type: 'friend' | 'group' | 'topic' | 'general';
  id?: number;
  name: string;
  roomId: string;
}

export default function Home() {
  const [user, setUser] = useState<any>(null);
  const [authMode, setAuthMode] = useState<AuthMode>('login');
  const [viewMode, setViewMode] = useState<ViewMode>('friends');
  const [showSignupSuccess, setShowSignupSuccess] = useState(false);
  const [chatTarget, setChatTarget] = useState<ChatTarget | null>(null);
  const [isAuthChecking, setIsAuthChecking] = useState(true);

  useEffect(() => {
    // 初回ロード時にローカルストレージから認証情報を確認
    const savedUser = authService.getCurrentUser();
    const token = localStorage.getItem('token');
    
    console.log('Auth check:', { savedUser, token, isAuthenticated: authService.isAuthenticated() });
    
    if (savedUser && token && authService.isAuthenticated()) {
      setUser(savedUser);
    } else {
      // トークンまたはユーザー情報が不完全な場合はクリア
      authService.logout();
      setUser(null);
    }
    
    setIsAuthChecking(false);
  }, []);

  const handleLogin = (userData: any) => {
    setUser(userData);
  };

  const handleLogout = () => {
    authService.logout();
    setUser(null);
  };

  const handleSignupSuccess = () => {
    setShowSignupSuccess(true);
    setTimeout(() => {
      setShowSignupSuccess(false);
      setAuthMode('login');
    }, 2000);
  };

  const handleSelectFriend = (friend: User, friendshipId: number) => {
    setChatTarget({
      type: 'friend',
      id: friendshipId,
      name: friend.displayName || friend.username,
      roomId: `friend-${friendshipId}`,
    });
    setViewMode('chat');
  };

  const handleSelectGroup = (group: Group) => {
    setChatTarget({
      type: 'group',
      id: group.id,
      name: group.name,
      roomId: `group-${group.id}`,
    });
    setViewMode('chat');
  };

  const handleSelectTopic = (topic: Topic) => {
    setChatTarget({
      type: 'topic',
      id: topic.id,
      name: topic.name,
      roomId: `topic-${topic.id}`,
    });
    setViewMode('chat');
  };

  const handleBackFromChat = () => {
    setChatTarget(null);
    setViewMode('friends');
  };

  // 認証チェック中
  if (isAuthChecking) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mx-auto mb-4"></div>
          <p className="text-gray-600">読み込み中...</p>
        </div>
      </div>
    );
  }

  if (!user) {
    if (showSignupSuccess) {
      return (
        <div className="min-h-screen flex items-center justify-center bg-gray-50">
          <div className="max-w-md w-full">
            <div className="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded text-center">
              <p className="font-bold">登録が完了しました!</p>
              <p>ログイン画面に移動します...</p>
            </div>
          </div>
        </div>
      );
    }

    if (authMode === 'signup') {
      return (
        <SignupForm
          onSignupSuccess={handleSignupSuccess}
          onSwitchToLogin={() => setAuthMode('login')}
        />
      );
    }

    return (
      <LoginForm
        onLogin={handleLogin}
        onSwitchToSignup={() => setAuthMode('signup')}
      />
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* ヘッダー */}
      <header className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 py-4 flex justify-between items-center">
          <h1 className="text-2xl font-bold text-gray-900">チャットアプリ</h1>
          <div className="flex items-center gap-4">
            <span className="text-gray-700">
              {user.displayName || user.username} さん
            </span>
            <button
              onClick={handleLogout}
              className="px-4 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600"
            >
              ログアウト
            </button>
          </div>
        </div>
      </header>

      <div className="max-w-7xl mx-auto px-4 py-6">
        {viewMode === 'chat' && chatTarget ? (
          <div>
            <button
              onClick={handleBackFromChat}
              className="mb-4 px-4 py-2 bg-gray-500 text-white rounded-lg hover:bg-gray-600"
            >
              ← 戻る
            </button>
            <div className="mb-4">
              <h2 className="text-2xl font-bold">{chatTarget.name}</h2>
              <p className="text-gray-600 text-sm">
                {chatTarget.type === 'friend' && 'フレンドチャット'}
                {chatTarget.type === 'group' && '招待制グループ'}
                {chatTarget.type === 'topic' && 'パブリックトピック'}
              </p>
            </div>
            <ChatRoom
              user={user}
              roomId={chatTarget.roomId}
            />
          </div>
        ) : (
          <>
            {/* ナビゲーションタブ */}
            <div className="flex gap-4 mb-6">
              <button
                onClick={() => setViewMode('friends')}
                className={`px-6 py-3 rounded-lg font-semibold ${
                  viewMode === 'friends'
                    ? 'bg-blue-500 text-white'
                    : 'bg-white text-gray-700 hover:bg-gray-100'
                }`}
              >
                👥 フレンド
              </button>
              <button
                onClick={() => setViewMode('groups')}
                className={`px-6 py-3 rounded-lg font-semibold ${
                  viewMode === 'groups'
                    ? 'bg-blue-500 text-white'
                    : 'bg-white text-gray-700 hover:bg-gray-100'
                }`}
              >
                🏘️ グループ & トピック
              </button>
            </div>

            {/* コンテンツエリア */}
            <div className="grid grid-cols-1 gap-6">
              {viewMode === 'friends' && (
                <FriendList onSelectFriend={handleSelectFriend} />
              )}
              {viewMode === 'groups' && (
                <GroupTopicList
                  onSelectGroup={handleSelectGroup}
                  onSelectTopic={handleSelectTopic}
                />
              )}
            </div>
          </>
        )}
      </div>
    </div>
  );
}
