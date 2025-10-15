'use client';

import { useState } from 'react';
import { authService, SignupRequest } from '@/services/api';

interface SignupFormProps {
  onSignupSuccess: () => void;
  onSwitchToLogin: () => void;
}

export default function SignupForm({ onSignupSuccess, onSwitchToLogin }: SignupFormProps) {
  const [userData, setUserData] = useState<SignupRequest>({
    username: '',
    email: '',
    password: '',
    displayName: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string>('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      await authService.signup(userData);
      onSignupSuccess();
    } catch (error: any) {
      setError(error.response?.data || '新規登録に失敗しました');
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setUserData({
      ...userData,
      [e.target.name]: e.target.value,
    });
  };

  return (
    <div className="min-h-screen flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8" style={{ backgroundColor: '#222831' }}>
      <div className="max-w-md w-full space-y-8">
        <div>
          <h2 className="mt-6 text-center text-3xl font-extrabold" style={{ color: '#EEEEEE' }}>
            新規登録
          </h2>
        </div>
        <form className="mt-8 space-y-6" onSubmit={handleSubmit}>
          {error && (
            <div className="px-4 py-3 rounded" style={{ backgroundColor: '#393E46', border: '1px solid #00ADB5', color: '#EEEEEE' }}>
              {error}
            </div>
          )}
          <div className="space-y-4">
            <div>
              <input
                id="username"
                name="username"
                type="text"
                required
                className="relative block w-full px-3 py-2 rounded-md focus:outline-none focus:z-10 sm:text-sm"
                style={{ backgroundColor: '#393E46', border: '1px solid #00ADB5', color: '#EEEEEE' }}
                placeholder="ユーザー名 (3文字以上)"
                value={userData.username}
                onChange={handleChange}
                minLength={3}
              />
            </div>
            <div>
              <input
                id="email"
                name="email"
                type="email"
                required
                className="relative block w-full px-3 py-2 rounded-md focus:outline-none focus:z-10 sm:text-sm"
                style={{ backgroundColor: '#393E46', border: '1px solid #00ADB5', color: '#EEEEEE' }}
                placeholder="メールアドレス"
                value={userData.email}
                onChange={handleChange}
              />
            </div>
            <div>
              <input
                id="displayName"
                name="displayName"
                type="text"
                className="relative block w-full px-3 py-2 rounded-md focus:outline-none focus:z-10 sm:text-sm"
                style={{ backgroundColor: '#393E46', border: '1px solid #00ADB5', color: '#EEEEEE' }}
                placeholder="表示名 (省略可)"
                value={userData.displayName}
                onChange={handleChange}
              />
            </div>
            <div>
              <input
                id="password"
                name="password"
                type="password"
                required
                className="relative block w-full px-3 py-2 rounded-md focus:outline-none focus:z-10 sm:text-sm"
                style={{ backgroundColor: '#393E46', border: '1px solid #00ADB5', color: '#EEEEEE' }}
                placeholder="パスワード (6文字以上)"
                value={userData.password}
                onChange={handleChange}
                minLength={6}
              />
            </div>
          </div>

          <div>
            <button
              type="submit"
              disabled={loading}
              className="group relative w-full flex justify-center py-2 px-4 text-sm font-medium rounded-md transition-opacity disabled:opacity-50"
              style={{ backgroundColor: '#00ADB5', color: '#EEEEEE' }}
              onMouseEnter={(e) => !loading && (e.currentTarget.style.opacity = '0.8')}
              onMouseLeave={(e) => e.currentTarget.style.opacity = '1'}
            >
              {loading ? '登録中...' : '新規登録'}
            </button>
          </div>

          <div className="text-center">
            <span className="text-sm" style={{ color: '#EEEEEE' }}>
              既にアカウントをお持ちの方は{' '}
              <button
                type="button"
                onClick={onSwitchToLogin}
                className="font-medium transition-opacity"
                style={{ color: '#00ADB5' }}
                onMouseEnter={(e) => e.currentTarget.style.opacity = '0.8'}
                onMouseLeave={(e) => e.currentTarget.style.opacity = '1'}
              >
                ログイン
              </button>
            </span>
          </div>
        </form>
      </div>
    </div>
  );
}