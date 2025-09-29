'use client';

import { useState, useEffect } from 'react';
import LoginForm from '@/components/LoginForm';
import SignupForm from '@/components/SignupForm';
import Dashboard from '@/components/Dashboard';
import { authService, StoredUser } from '@/services/api';

type AuthMode = 'login' | 'signup';

export default function Home() {
  const [user, setUser] = useState<StoredUser | null>(null);
  const [authMode, setAuthMode] = useState<AuthMode>('login');

  const [showSignupSuccess, setShowSignupSuccess] = useState(false);

  useEffect(() => {
    // 初回ロード時にローカルストレージから認証情報を確認
    const savedUser = authService.getCurrentUser();
    if (savedUser && authService.isAuthenticated()) {
      setUser(savedUser);
    }
  }, []);

  const handleLogin = (userData: StoredUser) => {
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

  if (user) {
    return (
      <Dashboard
        user={user}
        onLogout={handleLogout}
      />
    );
  }

  if (showSignupSuccess) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="max-w-md w-full">
          <div className="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded text-center">
            <p className="font-bold">登録が完了しました！</p>
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
