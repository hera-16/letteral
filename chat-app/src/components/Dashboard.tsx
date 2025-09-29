'use client';

import React, { useState, useEffect } from 'react';
import api, { StoredUser } from '@/services/api';

interface DashboardProps {
  user: StoredUser;
  onLogout: () => void;
}

interface Stats {
  friendCount: number;
  pendingRequestCount: number;
}

const Dashboard: React.FC<DashboardProps> = ({ user, onLogout }) => {
  const [stats, setStats] = useState<Stats>({ friendCount: 0, pendingRequestCount: 0 });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchStats();
  }, []);

  const fetchStats = async () => {
    try {
      const response = await api.get<Stats>('/friends/stats');
      setStats(response.data);
    } catch (error) {
      console.error('統計情報取得エラー:', error);
    } finally {
      setLoading(false);
    }
  };

  const navigationCards = [
    {
      title: 'フレンド管理',
      description: 'フレンドの追加・管理、申請の確認',
      icon: '👥',
      href: '/friends',
      stats: `${stats.friendCount}人のフレンド`,
      badge: stats.pendingRequestCount > 0 ? `${stats.pendingRequestCount}件の申請` : null,
      color: 'bg-blue-500'
    },
    {
      title: 'チャット',
      description: '通常のチャットルーム',
      icon: '💬',
      href: '/chat',
      stats: 'リアルタイムチャット',
      color: 'bg-green-500'
    },
    {
      title: 'Letteral (匿名グループ)',
      description: '匿名でのグループチャット',
      icon: '📱',
      href: '/groups',
      stats: '匿名投稿機能',
      color: 'bg-purple-500'
    }
  ];

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mx-auto mb-4"></div>
          <p className="text-gray-600">読み込み中...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* ヘッダー */}
      <div className="bg-white shadow-sm border-b border-gray-200">
        <div className="max-w-6xl mx-auto px-6 py-4">
          <div className="flex justify-between items-center">
            <div>
              <h1 className="text-2xl font-bold text-gray-900">
                チャットアプリ ダッシュボード
              </h1>
              <p className="text-gray-600 mt-1">
                {user.displayName || user.username} さん、おかえりなさい！
              </p>
            </div>
            <button
              onClick={onLogout}
              className="bg-red-500 text-white px-4 py-2 rounded hover:bg-red-600 transition-colors"
            >
              ログアウト
            </button>
          </div>
        </div>
      </div>

      {/* メインコンテンツ */}
      <div className="max-w-6xl mx-auto px-6 py-8">
        {/* ウェルカムメッセージ */}
        <div className="bg-white rounded-lg shadow-sm p-6 mb-8">
          <h2 className="text-xl font-semibold mb-2">ようこそ！</h2>
          <p className="text-gray-600">
            以下の機能から選択してください。フレンド機能を使って他のユーザーと繋がったり、
            匿名グループチャットで自由に会話を楽しんだりできます。
          </p>
        </div>

        {/* ナビゲーションカード */}
        <div className="grid md:grid-cols-3 gap-6">
          {navigationCards.map((card, index) => (
            <div
              key={index}
              className="bg-white rounded-lg shadow-sm hover:shadow-md transition-shadow cursor-pointer border border-gray-200"
              onClick={() => window.location.href = card.href}
            >
              <div className="p-6">
                <div className="flex items-center mb-4">
                  <div className={`${card.color} rounded-lg p-3 text-white text-2xl mr-4`}>
                    {card.icon}
                  </div>
                  <div className="flex-1">
                    <h3 className="text-lg font-semibold text-gray-900">
                      {card.title}
                    </h3>
                    {card.badge && (
                      <span className="inline-block bg-red-100 text-red-800 text-xs px-2 py-1 rounded-full mt-1">
                        {card.badge}
                      </span>
                    )}
                  </div>
                </div>
                <p className="text-gray-600 mb-3">{card.description}</p>
                <p className="text-sm text-gray-500">{card.stats}</p>
                <div className="mt-4 flex justify-end">
                  <span className="text-blue-500 hover:text-blue-600 font-medium">
                    開く →
                  </span>
                </div>
              </div>
            </div>
          ))}
        </div>

        {/* 最近の活動 */}
        <div className="mt-8 bg-white rounded-lg shadow-sm p-6">
          <h3 className="text-lg font-semibold mb-4">お知らせ</h3>
          <div className="space-y-3">
            <div className="flex items-start space-x-3">
              <div className="bg-blue-100 text-blue-600 rounded-full p-2">
                👋
              </div>
              <div>
                <p className="text-gray-800">チャットアプリへようこそ！</p>
                <p className="text-gray-500 text-sm">新機能：フレンド機能が追加されました</p>
              </div>
            </div>
            <div className="flex items-start space-x-3">
              <div className="bg-purple-100 text-purple-600 rounded-full p-2">
                🔒
              </div>
              <div>
                <p className="text-gray-800">Letteral 匿名グループチャット</p>
                <p className="text-gray-500 text-sm">完全匿名でプライベートな会話ができます</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;