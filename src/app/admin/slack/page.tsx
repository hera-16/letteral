'use client';

import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import {
  Slack,
  Plus,
  CheckCircle,
  XCircle,
  Power,
  Trash2,
  Send,
  Settings,
  AlertCircle,
  Info
} from 'lucide-react';

interface SlackIntegration {
  id: number;
  workspaceId: string;
  botToken: string;
  webhookUrl?: string;
  defaultChannel?: string;
  isActive: boolean;
  createdAt: string;
}

export default function SlackIntegrationPage() {
  const router = useRouter();
  const [integrations, setIntegrations] = useState<SlackIntegration[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [testingMessage, setTestingMessage] = useState(false);

  // フォームの状態
  const [formData, setFormData] = useState({
    workspaceId: '',
    botToken: '',
    webhookUrl: '',
    defaultChannel: '',
  });

  // テストメッセージの状態
  const [testMessage, setTestMessage] = useState({
    channel: '',
    message: 'これはLetteralからのテストメッセージです。',
  });

  useEffect(() => {
    fetchIntegrations();
  }, []);

  const fetchIntegrations = async () => {
    try {
      setLoading(true);
      const response = await fetch('/api/slack/integrations', {
        credentials: 'include',
      });

      if (!response.ok) throw new Error('Failed to fetch integrations');

      const data = await response.json();
      setIntegrations(data);
    } catch (error) {
      console.error('Error fetching integrations:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateIntegration = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.workspaceId || !formData.botToken) {
      alert('ワークスペースIDとBotトークンは必須です');
      return;
    }

    try {
      setSaving(true);
      const response = await fetch('/api/slack/integrations', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify(formData),
      });

      if (!response.ok) throw new Error('Failed to create integration');

      setShowForm(false);
      setFormData({
        workspaceId: '',
        botToken: '',
        webhookUrl: '',
        defaultChannel: '',
      });
      fetchIntegrations();
    } catch (error) {
      console.error('Error creating integration:', error);
      alert('Slack連携の作成に失敗しました');
    } finally {
      setSaving(false);
    }
  };

  const handleToggleActive = async (id: number, isActive: boolean) => {
    try {
      const response = await fetch(`/api/slack/integrations/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ isActive: !isActive }),
      });

      if (!response.ok) throw new Error('Failed to update integration');

      fetchIntegrations();
    } catch (error) {
      console.error('Error updating integration:', error);
      alert('連携の更新に失敗しました');
    }
  };

  const handleDeleteIntegration = async (id: number) => {
    if (!confirm('この連携を削除してもよろしいですか?')) return;

    try {
      const response = await fetch(`/api/slack/integrations/${id}`, {
        method: 'DELETE',
        credentials: 'include',
      });

      if (!response.ok) throw new Error('Failed to delete integration');

      fetchIntegrations();
    } catch (error) {
      console.error('Error deleting integration:', error);
      alert('連携の削除に失敗しました');
    }
  };

  const handleSendTestMessage = async () => {
    if (!testMessage.message.trim()) {
      alert('メッセージを入力してください');
      return;
    }

    try {
      setTestingMessage(true);
      const response = await fetch('/api/slack/test', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify(testMessage),
      });

      const result = await response.json();

      if (response.ok) {
        alert('テストメッセージを送信しました!');
      } else {
        alert(`送信に失敗しました: ${result.message}`);
      }
    } catch (error) {
      console.error('Error sending test message:', error);
      alert('テストメッセージの送信に失敗しました');
    } finally {
      setTestingMessage(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* ヘッダー */}
      <div className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <div className="flex justify-between items-center">
            <div className="flex items-center gap-3">
              <div className="bg-purple-100 rounded-lg p-3">
                <Slack className="w-8 h-8 text-purple-600" />
              </div>
              <div>
                <h1 className="text-2xl font-bold text-gray-900">Slack連携</h1>
                <p className="text-sm text-gray-600">Slackワークスペースとの連携を管理</p>
              </div>
            </div>
            <button
              onClick={() => router.push('/admin/dashboard')}
              className="px-4 py-2 text-sm bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition-colors"
            >
              ダッシュボードに戻る
            </button>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* 説明 */}
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-6 mb-6">
          <div className="flex items-start gap-4">
            <div className="bg-blue-100 rounded-lg p-2">
              <Info className="w-6 h-6 text-blue-600" />
            </div>
            <div className="flex-1">
              <h3 className="text-sm font-semibold text-blue-900 mb-2">Slack連携について</h3>
              <p className="text-sm text-blue-800 leading-relaxed">
                Slackと連携することで、進捗投稿や通報などの重要なイベントをSlackチャンネルに自動通知できます。
                リアルタイムでチームの活動を把握し、迅速な対応が可能になります。
              </p>
            </div>
          </div>
        </div>

        {/* 統計カード */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
          <div className="bg-white rounded-lg shadow-md p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600 mb-1">連携ワークスペース</p>
                <p className="text-3xl font-bold text-gray-900">{integrations.length}</p>
              </div>
              <Settings className="w-10 h-10 text-gray-400" />
            </div>
          </div>
          <div className="bg-white rounded-lg shadow-md p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600 mb-1">アクティブ</p>
                <p className="text-3xl font-bold text-green-600">
                  {integrations.filter(i => i.isActive).length}
                </p>
              </div>
              <CheckCircle className="w-10 h-10 text-green-600" />
            </div>
          </div>
          <div className="bg-white rounded-lg shadow-md p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600 mb-1">非アクティブ</p>
                <p className="text-3xl font-bold text-gray-600">
                  {integrations.filter(i => !i.isActive).length}
                </p>
              </div>
              <XCircle className="w-10 h-10 text-gray-600" />
            </div>
          </div>
        </div>

        {/* 連携追加ボタン */}
        <div className="mb-6">
          <button
            onClick={() => setShowForm(!showForm)}
            className="px-6 py-3 bg-purple-600 text-white rounded-lg hover:bg-purple-700 flex items-center gap-2 transition-colors shadow-md"
          >
            {showForm ? (
              <>
                <XCircle className="w-5 h-5" />
                キャンセル
              </>
            ) : (
              <>
                <Plus className="w-5 h-5" />
                新しい連携を追加
              </>
            )}
          </button>
        </div>

        {/* 連携フォーム */}
        {showForm && (
          <div className="bg-white rounded-lg shadow p-6 mb-6">
            <h2 className="text-lg font-semibold mb-4">Slack連携の設定</h2>
            <form onSubmit={handleCreateIntegration} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  ワークスペースID *
                </label>
                <input
                  type="text"
                  value={formData.workspaceId}
                  onChange={(e) => setFormData({ ...formData, workspaceId: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  placeholder="T01234567"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Bot User OAuth Token *
                </label>
                <input
                  type="password"
                  value={formData.botToken}
                  onChange={(e) => setFormData({ ...formData, botToken: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  placeholder="xoxb-..."
                  required
                />
                <p className="mt-1 text-xs text-gray-500">
                  Slack Appの「OAuth & Permissions」から取得できます
                </p>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Webhook URL (オプション)
                </label>
                <input
                  type="url"
                  value={formData.webhookUrl}
                  onChange={(e) => setFormData({ ...formData, webhookUrl: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  placeholder="https://hooks.slack.com/services/..."
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  デフォルトチャンネル
                </label>
                <input
                  type="text"
                  value={formData.defaultChannel}
                  onChange={(e) => setFormData({ ...formData, defaultChannel: e.target.value })}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  placeholder="#general"
                />
              </div>

              <div className="flex gap-3 pt-4">
                <button
                  type="submit"
                  disabled={saving}
                  className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-300"
                >
                  {saving ? '保存中...' : '連携を作成'}
                </button>
                <button
                  type="button"
                  onClick={() => setShowForm(false)}
                  className="px-6 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200"
                >
                  キャンセル
                </button>
              </div>
            </form>
          </div>
        )}

        {/* 連携リスト */}
        <div className="bg-white rounded-lg shadow-md">
          <div className="p-6">
            <div className="flex items-center gap-2 mb-6">
              <Slack className="w-6 h-6 text-purple-600" />
              <h2 className="text-xl font-semibold text-gray-900">連携一覧</h2>
            </div>
            {loading ? (
              <div className="text-center py-12">
                <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-purple-600 mx-auto"></div>
                <p className="mt-3 text-gray-600">読み込み中...</p>
              </div>
            ) : integrations.length === 0 ? (
              <div className="text-center py-12">
                <AlertCircle className="w-16 h-16 text-gray-300 mx-auto mb-3" />
                <p className="text-gray-500 text-lg">連携はまだ設定されていません</p>
                <p className="text-gray-400 text-sm mt-1">上のボタンから新しい連携を追加してください</p>
              </div>
            ) : (
              <div className="space-y-4">
                {integrations.map((integration) => (
                  <div
                    key={integration.id}
                    className="border-2 border-gray-200 rounded-lg p-5 hover:border-purple-200 transition-all"
                  >
                    <div className="flex justify-between items-start">
                      <div className="flex-1">
                        <div className="flex items-center gap-3 mb-3">
                          <div className="bg-purple-50 rounded-lg p-2">
                            <Slack className="w-5 h-5 text-purple-600" />
                          </div>
                          <div>
                            <h3 className="font-semibold text-gray-900 text-lg">
                              {integration.workspaceId}
                            </h3>
                            <span
                              className={`inline-flex items-center gap-1 px-3 py-1 rounded-full text-xs font-medium mt-1 ${
                                integration.isActive
                                  ? 'bg-green-100 text-green-800'
                                  : 'bg-gray-100 text-gray-800'
                              }`}
                            >
                              {integration.isActive ? (
                                <><CheckCircle className="w-3 h-3" /> 有効</>
                              ) : (
                                <><XCircle className="w-3 h-3" /> 無効</>
                              )}
                            </span>
                          </div>
                        </div>
                        <div className="ml-11 space-y-1">
                          {integration.defaultChannel && (
                            <p className="text-sm text-gray-600 flex items-center gap-1">
                              <span className="font-medium">デフォルトチャンネル:</span>
                              <span className="bg-gray-100 px-2 py-0.5 rounded">{integration.defaultChannel}</span>
                            </p>
                          )}
                          {integration.webhookUrl && (
                            <p className="text-sm text-gray-600">
                              <span className="font-medium">Webhook:</span> 設定済み
                            </p>
                          )}
                          <p className="text-xs text-gray-400 mt-2">
                            作成日: {new Date(integration.createdAt).toLocaleString('ja-JP')}
                          </p>
                        </div>
                      </div>
                      <div className="flex flex-col gap-2 ml-4">
                        <button
                          onClick={() => handleToggleActive(integration.id, integration.isActive)}
                          className={`px-4 py-2 text-sm rounded-lg flex items-center gap-2 transition-colors ${
                            integration.isActive
                              ? 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                              : 'bg-green-100 text-green-700 hover:bg-green-200'
                          }`}
                        >
                          <Power className="w-4 h-4" />
                          {integration.isActive ? '無効化' : '有効化'}
                        </button>
                        <button
                          onClick={() => handleDeleteIntegration(integration.id)}
                          className="px-4 py-2 text-sm bg-red-100 text-red-700 rounded-lg hover:bg-red-200 flex items-center gap-2 transition-colors"
                        >
                          <Trash2 className="w-4 h-4" />
                          削除
                        </button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* テストメッセージ送信 */}
        {integrations.some((i) => i.isActive) && (
          <div className="mt-6 bg-white rounded-lg shadow-md p-6">
            <div className="flex items-center gap-2 mb-6">
              <Send className="w-6 h-6 text-green-600" />
              <h2 className="text-xl font-semibold text-gray-900">テストメッセージ送信</h2>
            </div>
            <div className="space-y-5">
              <div>
                <label className="block text-sm font-semibold text-gray-700 mb-2">
                  チャンネル (オプション)
                </label>
                <input
                  type="text"
                  value={testMessage.channel}
                  onChange={(e) => setTestMessage({ ...testMessage, channel: e.target.value })}
                  className="w-full px-4 py-3 border-2 border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500 transition-all"
                  placeholder="#general (空欄の場合はデフォルトチャンネル)"
                />
                <p className="text-xs text-gray-500 mt-1">
                  チャンネル名を指定しない場合は、連携設定のデフォルトチャンネルに送信されます
                </p>
              </div>
              <div>
                <label className="block text-sm font-semibold text-gray-700 mb-2">
                  メッセージ
                </label>
                <textarea
                  value={testMessage.message}
                  onChange={(e) => setTestMessage({ ...testMessage, message: e.target.value })}
                  rows={4}
                  className="w-full px-4 py-3 border-2 border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-green-500 transition-all"
                  placeholder="テストメッセージを入力してください..."
                />
              </div>
              <button
                onClick={handleSendTestMessage}
                disabled={testingMessage || !testMessage.message.trim()}
                className="w-full px-6 py-3 bg-green-600 text-white rounded-lg hover:bg-green-700 disabled:bg-gray-300 disabled:cursor-not-allowed flex items-center justify-center gap-2 transition-colors shadow-md"
              >
                {testingMessage ? (
                  <>
                    <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-white"></div>
                    送信中...
                  </>
                ) : (
                  <>
                    <Send className="w-5 h-5" />
                    テストメッセージを送信
                  </>
                )}
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
