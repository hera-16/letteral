'use client';

import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import api from '@/services/api';

interface InviteInfo {
  id: number;
  organizationId: number;
  organizationName: string;
  inviteCode: string;
  defaultRole: string;
  maxUses: number | null;
  useCount: number;
  expiresAt: string | null;
  isValid: boolean;
}

export default function InvitePage({ params }: { params: Promise<{ inviteCode: string }> }) {
  const router = useRouter();
  const [inviteInfo, setInviteInfo] = useState<InviteInfo | null>(null);
  const [loading, setLoading] = useState(true);
  const [accepting, setAccepting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [inviteCode, setInviteCode] = useState<string>('');

  useEffect(() => {
    params.then((p) => {
      setInviteCode(p.inviteCode);
    });
  }, []);

  useEffect(() => {
    if (inviteCode) {
      loadInviteInfo();
    }
  }, [inviteCode]);

  const loadInviteInfo = async () => {
    try {
      setLoading(true);
      const response = await api.get(`/organization-invites/${inviteCode}`);
      setInviteInfo(response.data);
      setError(null);
    } catch (err: any) {
      setError('招待リンクが見つからないか、無効です');
      console.error('Failed to load invite:', err);
    } finally {
      setLoading(false);
    }
  };

  const acceptInvite = async () => {
    if (!inviteInfo) return;

    try {
      setAccepting(true);
      await api.post(`/organization-invites/${inviteCode}/accept`, {});

      alert(`${inviteInfo.organizationName}への参加が完了しました！`);
      router.push('/');
    } catch (err: any) {
      setError(err.response?.data?.error || '招待の受け入れに失敗しました');
      console.error('Failed to accept invite:', err);
    } finally {
      setAccepting(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
          <p className="text-gray-600">読み込み中...</p>
        </div>
      </div>
    );
  }

  if (error || !inviteInfo) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">
        <div className="max-w-md w-full bg-white rounded-lg shadow-lg p-8">
          <div className="text-center">
            <div className="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-red-100 mb-4">
              <svg className="h-6 w-6 text-red-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </div>
            <h2 className="text-2xl font-bold text-gray-900 mb-2">招待リンクが無効です</h2>
            <p className="text-gray-600 mb-6">{error}</p>
            <button
              onClick={() => router.push('/')}
              className="px-6 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
            >
              ホームに戻る
            </button>
          </div>
        </div>
      </div>
    );
  }

  if (!inviteInfo.isValid) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">
        <div className="max-w-md w-full bg-white rounded-lg shadow-lg p-8">
          <div className="text-center">
            <div className="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-yellow-100 mb-4">
              <svg className="h-6 w-6 text-yellow-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
              </svg>
            </div>
            <h2 className="text-2xl font-bold text-gray-900 mb-2">招待リンクが無効です</h2>
            <p className="text-gray-600 mb-6">
              この招待リンクは期限切れか、使用回数の上限に達しています。
            </p>
            <button
              onClick={() => router.push('/')}
              className="px-6 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
            >
              ホームに戻る
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">
      <div className="max-w-md w-full bg-white rounded-lg shadow-lg p-8">
        <div className="text-center mb-6">
          <div className="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-blue-100 mb-4">
            <svg className="h-6 w-6 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
            </svg>
          </div>
          <h2 className="text-2xl font-bold text-gray-900 mb-2">組織への招待</h2>
          <p className="text-gray-600">
            以下の組織への参加が招待されています
          </p>
        </div>

        <div className="bg-gray-50 rounded-lg p-6 mb-6">
          <div className="space-y-3">
            <div>
              <div className="text-sm text-gray-500">組織名</div>
              <div className="text-lg font-semibold text-gray-900">
                {inviteInfo.organizationName}
              </div>
            </div>

            <div>
              <div className="text-sm text-gray-500">付与される権限</div>
              <div className="text-lg font-semibold text-gray-900">
                {inviteInfo.defaultRole === 'ADMIN' ? '管理者' :
                 inviteInfo.defaultRole === 'MANAGER' ? 'マネージャー' : 'メンバー'}
              </div>
            </div>

            {inviteInfo.expiresAt && (
              <div>
                <div className="text-sm text-gray-500">招待の有効期限</div>
                <div className="text-md text-gray-700">
                  {new Date(inviteInfo.expiresAt).toLocaleDateString('ja-JP')}
                </div>
              </div>
            )}

            {inviteInfo.maxUses && (
              <div>
                <div className="text-sm text-gray-500">残り使用回数</div>
                <div className="text-md text-gray-700">
                  {inviteInfo.maxUses - inviteInfo.useCount}回
                </div>
              </div>
            )}
          </div>
        </div>

        <div className="space-y-3">
          <button
            onClick={acceptInvite}
            disabled={accepting}
            className="w-full px-6 py-3 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:bg-gray-400 font-medium"
          >
            {accepting ? '参加中...' : 'この組織に参加する'}
          </button>

          <button
            onClick={() => router.push('/')}
            disabled={accepting}
            className="w-full px-6 py-3 bg-gray-200 text-gray-700 rounded-md hover:bg-gray-300 disabled:bg-gray-100"
          >
            キャンセル
          </button>
        </div>
      </div>
    </div>
  );
}
