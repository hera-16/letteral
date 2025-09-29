'use client';

import { useState } from 'react';
import { authService, LoginRequest, StoredUser } from '@/services/api';

interface LoginFormProps {
  onLogin: (user: StoredUser) => void;
  onSwitchToSignup: () => void;
}

export default function LoginForm({ onLogin, onSwitchToSignup }: LoginFormProps) {
  const [credentials, setCredentials] = useState<LoginRequest>({
    username: '',
    password: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string>('');

  const extractErrorMessage = (err: unknown): string => {
    if (typeof err === 'string') {
      return err;
    }
    if (err && typeof err === 'object') {
      const maybeResponse = (err as { response?: { data?: unknown } }).response;
      if (maybeResponse?.data) {
        if (typeof maybeResponse.data === 'string') {
          return maybeResponse.data;
        }
        if (
          typeof maybeResponse.data === 'object' &&
          maybeResponse.data !== null &&
          'message' in maybeResponse.data &&
          typeof (maybeResponse.data as { message: unknown }).message === 'string'
        ) {
          return (maybeResponse.data as { message: string }).message;
        }
      }
      if ('message' in err && typeof (err as { message: unknown }).message === 'string') {
        return (err as { message: string }).message;
      }
    }
    return 'ログインに失敗しました';
  };

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const userProfile = await authService.login(credentials);
      onLogin(userProfile);
    } catch (error: unknown) {
      setError(extractErrorMessage(error));
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
    <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8">
        <div>
          <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">
            ログイン
          </h2>
        </div>
        <form className="mt-8 space-y-6" onSubmit={handleSubmit}>
          {error && (
            <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
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
                className="relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-t-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 focus:z-10 sm:text-sm"
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
                className="relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-b-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 focus:z-10 sm:text-sm"
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
              className="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50"
            >
              {loading ? 'ログイン中...' : 'ログイン'}
            </button>
          </div>

          <div className="text-center">
            <span className="text-sm text-gray-600">
              アカウントをお持ちでない方は{' '}
              <button
                type="button"
                onClick={onSwitchToSignup}
                className="font-medium text-indigo-600 hover:text-indigo-500"
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