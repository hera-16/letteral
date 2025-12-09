'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import LoginForm from '@/components/LoginForm';
import { authService } from '@/services/api';

export default function LoginPage() {
  const router = useRouter();
  const [isChecking, setIsChecking] = useState(true);

  useEffect(() => {
    // すでにログインしている場合はホームへリダイレクト
    const savedUser = authService.getCurrentUser();
    const token = localStorage.getItem('token');

    if (savedUser && token && authService.isAuthenticated()) {
      router.push('/');
    }
    setIsChecking(false);
  }, [router]);

  const handleLogin = (userData: any) => {
    // ログイン成功後、ホームページへリダイレクト
    router.push('/');
  };

  const handleSwitchToSignup = () => {
    router.push('/signup');
  };

  if (isChecking) {
    return (
      <div className="min-h-screen flex items-center justify-center" style={{ backgroundColor: '#222831' }}>
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 mx-auto mb-4" style={{ borderColor: '#00ADB5' }}></div>
          <p style={{ color: '#EEEEEE' }}>読み込み中...</p>
        </div>
      </div>
    );
  }

  return (
    <>
      <title>ログイン - Letteral</title>
      <LoginForm onLogin={handleLogin} onSwitchToSignup={handleSwitchToSignup} />
    </>
  );
}
