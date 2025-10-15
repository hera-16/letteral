'use client';

import { useState, useEffect, useRef } from 'react';
import LoginForm from '@/components/LoginForm';
import SignupForm from '@/components/SignupForm';
import FriendList from '@/components/FriendList';
import GroupList from '@/components/GroupList';
import ChatRoom from '@/components/ChatRoom';
import GroupSettings from '@/components/GroupSettings';
import DailyChallenges from '@/components/DailyChallenges';
import ChallengeShareTimeline from '@/components/ChallengeShareTimeline';
import Badges from '@/components/Badges';
import { authService, User, Group } from '@/services/api';

type AuthMode = 'login' | 'signup';
type ViewMode = 'friends' | 'groups' | 'challenges' | 'chat' | 'badges';

interface ChatTarget {
  type: 'friend' | 'group' | 'general';
  id?: number;
  name: string;
  roomId: string;
}

export default function Home() {
  const [user, setUser] = useState<any>(null);
  const [authMode, setAuthMode] = useState<AuthMode>('login');
  const [viewMode, setViewMode] = useState<ViewMode>('challenges');
  const [showSignupSuccess, setShowSignupSuccess] = useState(false);
  const [chatTarget, setChatTarget] = useState<ChatTarget | null>(null);
  const [isAuthChecking, setIsAuthChecking] = useState(true);
  const [showGroupSettings, setShowGroupSettings] = useState(false);
  const [pendingShareChallengeId, setPendingShareChallengeId] = useState<number | null>(null);
  const timelineSectionRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    // 初回ロード時にローカルストレージから認証情報を確認
    const savedUser = authService.getCurrentUser();
    const token = localStorage.getItem('token');
    
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

  const handleBackFromChat = () => {
    setChatTarget(null);
    setViewMode('challenges');
  };

  const handleRequestShare = (challengeId: number) => {
    setViewMode('challenges');
    setPendingShareChallengeId(challengeId);
    setTimeout(() => {
      timelineSectionRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }, 150);
  };

  // 認証チェック中
  if (isAuthChecking) {
    return (
      <div className="min-h-screen flex items-center justify-center" style={{ backgroundColor: '#222831' }}>
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 mx-auto mb-4" style={{ borderColor: '#00ADB5' }}></div>
          <p style={{ color: '#EEEEEE' }}>読み込み中...</p>
        </div>
      </div>
    );
  }

  if (!user) {
    if (showSignupSuccess) {
      return (
        <div className="min-h-screen flex items-center justify-center" style={{ backgroundColor: '#222831' }}>
          <div className="max-w-md w-full">
            <div className="px-4 py-3 rounded text-center" style={{ backgroundColor: '#00ADB5', color: '#EEEEEE' }}>
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
    <div className="min-h-screen" style={{ backgroundColor: '#222831' }}>
      {/* ヘッダー */}
      <header className="shadow" style={{ backgroundColor: '#393E46' }}>
        <div className="max-w-7xl mx-auto px-4 py-4 flex justify-between items-center">
          <h1 className="text-2xl font-bold" style={{ color: '#EEEEEE' }}>チャットアプリ</h1>
          <div className="flex items-center gap-4">
            <span style={{ color: '#EEEEEE' }}>
              {user.displayName || user.username} さん
            </span>
            <button
              onClick={handleLogout}
              className="px-4 py-2 rounded-lg transition-colors"
              style={{ backgroundColor: '#00ADB5', color: '#EEEEEE' }}
              onMouseEnter={(e) => e.currentTarget.style.opacity = '0.8'}
              onMouseLeave={(e) => e.currentTarget.style.opacity = '1'}
            >
              ログアウト
            </button>
          </div>
        </div>
      </header>

      <div className="max-w-7xl mx-auto px-4 py-6">
        {viewMode === 'chat' && chatTarget ? (
          <div>
            <div className="flex gap-4 mb-4">
              <button
                onClick={handleBackFromChat}
                className="px-4 py-2 rounded-lg transition-opacity"
                style={{ backgroundColor: '#393E46', color: '#EEEEEE' }}
                onMouseEnter={(e) => e.currentTarget.style.opacity = '0.8'}
                onMouseLeave={(e) => e.currentTarget.style.opacity = '1'}
              >
                ← 戻る
              </button>
              {chatTarget.type === 'group' && (
                <button
                  onClick={() => setShowGroupSettings(true)}
                  className="px-4 py-2 rounded-lg transition-opacity"
                  style={{ backgroundColor: '#00ADB5', color: '#EEEEEE' }}
                  onMouseEnter={(e) => e.currentTarget.style.opacity = '0.8'}
                  onMouseLeave={(e) => e.currentTarget.style.opacity = '1'}
                >
                  ⚙️ グループ設定
                </button>
              )}
            </div>
            <div className="mb-4">
              <h2 className="text-2xl font-bold" style={{ color: '#EEEEEE' }}>{chatTarget.name}</h2>
              <p className="text-sm" style={{ color: '#00ADB5' }}>
                {chatTarget.type === 'friend' && 'フレンドチャット'}
                {chatTarget.type === 'group' && '匿名グループチャット'}
              </p>
            </div>
            <ChatRoom
              user={user}
              roomId={chatTarget.roomId}
              chatType={chatTarget.type}
              chatId={chatTarget.id}
            />
            {showGroupSettings && chatTarget.type === 'group' && chatTarget.id && (
              <GroupSettings
                groupId={chatTarget.id}
                onClose={() => setShowGroupSettings(false)}
                onUpdate={() => {
                  // グループ情報を再読み込み
                  setShowGroupSettings(false);
                }}
              />
            )}
          </div>
        ) : (
          <>
            {/* ナビゲーションタブ */}
            <div className="flex gap-4 mb-6">
              <button
                onClick={() => setViewMode('challenges')}
                className="px-6 py-3 rounded-lg font-semibold transition-all"
                style={{
                  backgroundColor: viewMode === 'challenges' ? '#00ADB5' : '#393E46',
                  color: '#EEEEEE'
                }}
              >
                🌸 デイリーチャレンジ
              </button>
              <button
                onClick={() => setViewMode('badges')}
                className="px-6 py-3 rounded-lg font-semibold transition-all"
                style={{
                  backgroundColor: viewMode === 'badges' ? '#00ADB5' : '#393E46',
                  color: '#EEEEEE'
                }}
              >
                🏆 獲得バッジ
              </button>
              <button
                onClick={() => setViewMode('friends')}
                className="px-6 py-3 rounded-lg font-semibold transition-all"
                style={{
                  backgroundColor: viewMode === 'friends' ? '#00ADB5' : '#393E46',
                  color: '#EEEEEE'
                }}
              >
                👥 フレンド
              </button>
              <button
                onClick={() => setViewMode('groups')}
                className="px-6 py-3 rounded-lg font-semibold transition-all"
                style={{
                  backgroundColor: viewMode === 'groups' ? '#00ADB5' : '#393E46',
                  color: '#EEEEEE'
                }}
              >
                🏘️ 匿名グループ
              </button>
            </div>

            {/* コンテンツエリア */}
            <div className="grid grid-cols-1 gap-6">
              {viewMode === 'challenges' && (
                <div className="grid grid-cols-1 xl:grid-cols-[minmax(0,1.2fr)_minmax(0,1fr)] gap-6 items-start">
                  <div>
                    <DailyChallenges onRequestShare={handleRequestShare} />
                  </div>
                  <div ref={timelineSectionRef} className="scroll-mt-24">
                    <ChallengeShareTimeline
                      pendingShareChallengeId={pendingShareChallengeId}
                      onSharePosted={() => setPendingShareChallengeId(null)}
                      onPendingShareConsumed={() => setPendingShareChallengeId(null)}
                    />
                  </div>
                </div>
              )}
              {viewMode === 'badges' && (
                <Badges />
              )}
              {viewMode === 'friends' && (
                <FriendList onSelectFriend={handleSelectFriend} />
              )}
              {viewMode === 'groups' && (
                <GroupList onSelectGroup={handleSelectGroup} />
              )}
            </div>
          </>
        )}
      </div>
    </div>
  );
}
