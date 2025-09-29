'use client';

import { useState, useEffect } from 'react';
import ChatRoom from '@/components/ChatRoom';
import { authService, StoredUser } from '@/services/api';

export default function ChatPage() {
  const [user, setUser] = useState<StoredUser | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const savedUser = authService.getCurrentUser();
    if (savedUser && authService.isAuthenticated()) {
      setUser(savedUser);
    } else {
      // 認証されていない場合はログインページにリダイレクト
      window.location.href = '/';
    }
    setLoading(false);
  }, []);

  const handleLogout = () => {
    authService.logout();
    window.location.href = '/';
  };

  if (loading) {
    return <div className="flex justify-center items-center h-64">読み込み中...</div>;
  }

  if (!user) {
    return <div className="flex justify-center items-center h-64">認証が必要です</div>;
  }

  return (
    <ChatRoom
      user={user}
      roomId="general"
      onLogout={handleLogout}
    />
  );
}