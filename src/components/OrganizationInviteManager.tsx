'use client';

import React, { useState, useEffect } from 'react';
import api from '@/services/api';

interface OrganizationInvite {
  id: number;
  organizationId: number;
  organizationName: string;
  inviteCode: string;
  inviteUrl: string;
  defaultRole: string;
  maxUses: number | null;
  useCount: number;
  expiresAt: string | null;
  isActive: boolean;
  isValid: boolean;
  createdAt: string;
}

interface OrganizationInviteManagerProps {
  organizationId: number;
}

const OrganizationInviteManager: React.FC<OrganizationInviteManagerProps> = ({ organizationId }) => {
  const [invites, setInvites] = useState<OrganizationInvite[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [showCreateForm, setShowCreateForm] = useState(false);

  const [formData, setFormData] = useState({
    defaultRole: 'MEMBER',
    maxUses: '',
    expiresInDays: '30'
  });

  useEffect(() => {
    loadInvites();
  }, [organizationId]);

  const loadInvites = async () => {
    try {
      setLoading(true);
      const response = await api.get(`/organization-invites/organization/${organizationId}`);
      setInvites(response.data);
      setError(null);
    } catch (err: any) {
      setError('招待一覧の読み込みに失敗しました');
      console.error('Failed to load invites:', err);
    } finally {
      setLoading(false);
    }
  };

  const createInvite = async () => {
    try {
      setLoading(true);
      const payload: any = {
        organizationId,
        defaultRole: formData.defaultRole,
      };

      if (formData.maxUses) {
        payload.maxUses = parseInt(formData.maxUses);
      }

      if (formData.expiresInDays) {
        payload.expiresInDays = parseInt(formData.expiresInDays);
      }

      await api.post('/organization-invites', payload);

      setShowCreateForm(false);
      setFormData({ defaultRole: 'MEMBER', maxUses: '', expiresInDays: '30' });
      await loadInvites();
      setError(null);
    } catch (err: any) {
      setError('招待リンクの作成に失敗しました');
      console.error('Failed to create invite:', err);
    } finally {
      setLoading(false);
    }
  };

  const deactivateInvite = async (inviteId: number) => {
    if (!confirm('この招待リンクを無効化しますか？')) return;

    try {
      setLoading(true);
      await api.delete(`/organization-invites/${inviteId}`);
      await loadInvites();
      setError(null);
    } catch (err: any) {
      setError('招待リンクの無効化に失敗しました');
      console.error('Failed to deactivate invite:', err);
    } finally {
      setLoading(false);
    }
  };

  const copyToClipboard = (text: string) => {
    navigator.clipboard.writeText(text);
    alert('招待URLをコピーしました');
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h2 className="text-2xl font-bold text-gray-900">招待リンク管理</h2>
        <button
          onClick={() => setShowCreateForm(!showCreateForm)}
          className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
          disabled={loading}
        >
          {showCreateForm ? 'キャンセル' : '新規作成'}
        </button>
      </div>

      {error && (
        <div className="p-4 bg-red-50 border border-red-200 rounded-md text-red-600">
          {error}
        </div>
      )}

      {showCreateForm && (
        <div className="p-6 bg-white border border-gray-200 rounded-lg shadow-sm">
          <h3 className="text-lg font-semibold mb-4">新しい招待リンクを作成</h3>

          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                デフォルト権限
              </label>
              <select
                value={formData.defaultRole}
                onChange={(e) => setFormData({ ...formData, defaultRole: e.target.value })}
                className="w-full px-3 py-2 border border-gray-300 rounded-md"
              >
                <option value="MEMBER">メンバー</option>
                <option value="MANAGER">マネージャー</option>
                <option value="ADMIN">管理者</option>
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                最大使用回数（オプション）
              </label>
              <input
                type="number"
                value={formData.maxUses}
                onChange={(e) => setFormData({ ...formData, maxUses: e.target.value })}
                placeholder="無制限"
                className="w-full px-3 py-2 border border-gray-300 rounded-md"
                min="1"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                有効期限（日数）
              </label>
              <input
                type="number"
                value={formData.expiresInDays}
                onChange={(e) => setFormData({ ...formData, expiresInDays: e.target.value })}
                placeholder="30"
                className="w-full px-3 py-2 border border-gray-300 rounded-md"
                min="1"
              />
            </div>

            <button
              onClick={createInvite}
              disabled={loading}
              className="w-full px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:bg-gray-400"
            >
              {loading ? '作成中...' : '招待リンクを作成'}
            </button>
          </div>
        </div>
      )}

      <div className="space-y-4">
        <h3 className="text-lg font-semibold">アクティブな招待リンク</h3>

        {loading && invites.length === 0 ? (
          <div className="text-center py-8 text-gray-500">読み込み中...</div>
        ) : invites.length === 0 ? (
          <div className="text-center py-8 text-gray-500">
            招待リンクがありません
          </div>
        ) : (
          <div className="grid gap-4">
            {invites.map((invite) => (
              <div
                key={invite.id}
                className="p-4 bg-white border border-gray-200 rounded-lg shadow-sm"
              >
                <div className="flex justify-between items-start mb-3">
                  <div>
                    <div className="font-medium text-gray-900">
                      {invite.defaultRole === 'ADMIN' ? '管理者' :
                       invite.defaultRole === 'MANAGER' ? 'マネージャー' : 'メンバー'}
                      権限
                    </div>
                    <div className="text-sm text-gray-500">
                      作成日: {new Date(invite.createdAt).toLocaleDateString('ja-JP')}
                    </div>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className={`px-2 py-1 text-xs rounded-full ${
                      invite.isValid
                        ? 'bg-green-100 text-green-800'
                        : 'bg-red-100 text-red-800'
                    }`}>
                      {invite.isValid ? '有効' : '無効'}
                    </span>
                  </div>
                </div>

                <div className="space-y-2 mb-3">
                  {invite.maxUses && (
                    <div className="text-sm text-gray-600">
                      使用回数: {invite.useCount} / {invite.maxUses}
                    </div>
                  )}
                  {invite.expiresAt && (
                    <div className="text-sm text-gray-600">
                      有効期限: {new Date(invite.expiresAt).toLocaleDateString('ja-JP')}
                    </div>
                  )}
                </div>

                <div className="flex items-center gap-2 p-2 bg-gray-50 rounded border border-gray-200 mb-3">
                  <input
                    type="text"
                    value={invite.inviteUrl}
                    readOnly
                    className="flex-1 bg-transparent border-none text-sm text-gray-700 focus:outline-none"
                  />
                  <button
                    onClick={() => copyToClipboard(invite.inviteUrl)}
                    className="px-3 py-1 bg-blue-600 text-white text-sm rounded hover:bg-blue-700"
                  >
                    コピー
                  </button>
                </div>

                <button
                  onClick={() => deactivateInvite(invite.id)}
                  disabled={loading}
                  className="w-full px-4 py-2 bg-red-600 text-white text-sm rounded hover:bg-red-700 disabled:bg-gray-400"
                >
                  無効化
                </button>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default OrganizationInviteManager;
