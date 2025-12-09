'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import SignupForm from '@/components/SignupForm';
import { authService } from '@/services/api';

export default function SignupPage() {
  const router = useRouter();
  const [isChecking, setIsChecking] = useState(true);
  const [showSuccess, setShowSuccess] = useState(false);

  useEffect(() => {
    // すでにログインしている場合はホームへリダイレクト
    const savedUser = authService.getCurrentUser();
    const token = localStorage.getItem('token');

    if (savedUser && token && authService.isAuthenticated()) {
      router.push('/');
    }
    setIsChecking(false);
  }, [router]);

  const handleSignupSuccess = () => {
    setShowSuccess(true);
    setTimeout(() => {
      router.push('/login');
    }, 2000);
  };

  const handleSwitchToLogin = () => {
    router.push('/login');
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

  if (showSuccess) {
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

  return (
    <>
      <title>新規登録 - Letteral</title>
      <SignupForm onSignupSuccess={handleSignupSuccess} onSwitchToLogin={handleSwitchToLogin} />
    </>
  );
}
