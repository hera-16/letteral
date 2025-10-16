'use client';

import { useState } from 'react';
import { authService, LoginRequest } from '@/services/api';

interface LoginFormProps {
  onLogin: (user: any) => void;
  onSwitchToSignup: () => void;
}

export default function LoginForm({ onLogin, onSwitchToSignup }: LoginFormProps) {
  const [credentials, setCredentials] = useState<LoginRequest>({
    username: '',
    password: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string>('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const response = await authService.login(credentials);
      
      console.log('🔐 Login successful - Response:', {
        tokenLength: response.accessToken?.length,
        tokenPreview: response.accessToken?.substring(0, 30),
        username: response.username
      });
      
      // トークンとユーザー情報をローカルストレージに保存
      localStorage.setItem('token', response.accessToken);
      localStorage.setItem('user', JSON.stringify({
        id: response.id,
        username: response.username,
        email: response.email,
        displayName: response.displayName,
      }));
      
      // 保存確認
      const savedToken = localStorage.getItem('token');
      console.log('💾 Token saved to localStorage:', {
        saved: !!savedToken,
        length: savedToken?.length,
        matches: savedToken === response.accessToken
      });

      onLogin(response);
    } catch (error: any) {
      console.error('❌ Login failed:', error);
      setError(error.response?.data?.error || error.response?.data || 'ログインに失敗しました');
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setCredentials({
      ...credentials,
      [e.target.name]: e.target.value,
    });
  };

  return (
    <div className="min-h-screen flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8" style={{ backgroundColor: '#222831' }}>
      <div className="max-w-md w-full space-y-8">
        <div>
          <h2 className="mt-6 text-center text-3xl font-extrabold" style={{ color: '#EEEEEE' }}>
            ログイン
          </h2>
        </div>
        <form className="mt-8 space-y-6" onSubmit={handleSubmit}>
          {error && (
            <div className="px-4 py-3 rounded" style={{ backgroundColor: '#393E46', border: '1px solid #00ADB5', color: '#EEEEEE' }}>
              {error}
            </div>
          )}
          <div className="rounded-md shadow-sm -space-y-px">
            <div>
              <input
                id="username"
                name="username"
                type="text"
                required
                className="relative block w-full px-3 py-2 rounded-t-md focus:outline-none focus:z-10 sm:text-sm"
                style={{ backgroundColor: '#393E46', border: '1px solid #00ADB5', color: '#EEEEEE' }}
                placeholder="ユーザー名"
                value={credentials.username}
                onChange={handleChange}
              />
            </div>
            <div>
              <input
                id="password"
                name="password"
                type="password"
                required
                className="relative block w-full px-3 py-2 rounded-b-md focus:outline-none focus:z-10 sm:text-sm"
                style={{ backgroundColor: '#393E46', border: '1px solid #00ADB5', color: '#EEEEEE' }}
                placeholder="パスワード"
                value={credentials.password}
                onChange={handleChange}
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
              {loading ? 'ログイン中...' : 'ログイン'}
            </button>
          </div>

          <div className="text-center">
            <span className="text-sm" style={{ color: '#EEEEEE' }}>
              アカウントをお持ちでない方は{' '}
              <button
                type="button"
                onClick={onSwitchToSignup}
                className="font-medium transition-opacity"
                style={{ color: '#00ADB5' }}
                onMouseEnter={(e) => e.currentTarget.style.opacity = '0.8'}
                onMouseLeave={(e) => e.currentTarget.style.opacity = '1'}
              >
                新規登録
              </button>
            </span>
          </div>
        </form>
      </div>
    </div>
  );
}