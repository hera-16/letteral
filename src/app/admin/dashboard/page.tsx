'use client';

import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import {
  Users,
  FileText,
  MessageSquare,
  Target,
  AlertTriangle,
  Layers,
  TrendingUp,
  Activity,
  Calendar,
  BarChart3
} from 'lucide-react';

interface DashboardStats {
  users: {
    total: number;
    active: number;
  };
  posts: {
    total: number;
    recentWeek: number;
  };
  messages: {
    total: number;
  };
  okrs: {
    total: number;
    completed: number;
    inProgress: number;
    completionRate: number;
  };
  reports: {
    pending: number;
  };
  groups: {
    total: number;
  };
}

interface ActivityData {
  date: string;
  posts: number;
  messages: number;
}

export default function AdminDashboard() {
  const router = useRouter();
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [activityData, setActivityData] = useState<ActivityData[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [timeRange, setTimeRange] = useState<'7d' | '30d' | '90d'>('7d');

  useEffect(() => {
    fetchDashboardStats();
    fetchActivityData();
  }, [timeRange]);

  const fetchDashboardStats = async () => {
    try {
      const response = await fetch('/api/admin/dashboard/statistics', {
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include',
      });

      if (!response.ok) {
        throw new Error('Failed to fetch dashboard statistics');
      }

      const data = await response.json();
      setStats(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An error occurred');
    } finally {
      setLoading(false);
    }
  };

  const fetchActivityData = async () => {
    try {
      const response = await fetch(`/api/admin/dashboard/activity?range=${timeRange}`, {
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include',
      });

      if (response.ok) {
        const data = await response.json();
        setActivityData(data);
      }
    } catch (err) {
      console.error('Failed to fetch activity data:', err);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">読み込み中...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <p className="text-red-600">{error}</p>
          <button
            onClick={fetchDashboardStats}
            className="mt-4 px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
          >
            再試行
          </button>
        </div>
      </div>
    );
  }

  if (!stats) return null;

  return (
    <div className="min-h-screen bg-gray-50">
      {/* ヘッダー */}
      <div className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="flex justify-between items-center">
            <h1 className="text-2xl font-bold text-gray-900">管理者ダッシュボード</h1>
            <div className="flex gap-2">
              <button
                onClick={() => router.push('/admin/reports')}
                className="px-4 py-2 text-sm bg-red-50 text-red-700 rounded hover:bg-red-100"
              >
                通報管理
              </button>
              <button
                onClick={() => router.push('/admin/slack')}
                className="px-4 py-2 text-sm bg-green-50 text-green-700 rounded hover:bg-green-100"
              >
                Slack連携
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* 統計カード */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {/* ユーザー統計 */}
          <div className="bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition-shadow">
            <div className="flex items-center justify-between">
              <div>
                <div className="flex items-center gap-2 mb-2">
                  <Users className="w-5 h-5 text-gray-500" />
                  <p className="text-sm font-medium text-gray-600">総ユーザー数</p>
                </div>
                <p className="text-3xl font-bold text-gray-900">{stats.users.total}</p>
                <p className="text-sm text-green-600 mt-1">
                  <span className="font-medium">アクティブ: {stats.users.active}</span>
                  <span className="text-gray-400 ml-1">
                    ({stats.users.total > 0 ? ((stats.users.active / stats.users.total) * 100).toFixed(1) : 0}%)
                  </span>
                </p>
              </div>
              <div className="bg-blue-50 rounded-full p-4">
                <Users className="w-8 h-8 text-blue-600" />
              </div>
            </div>
          </div>

          {/* 進捗投稿統計 */}
          <div className="bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition-shadow">
            <div className="flex items-center justify-between">
              <div>
                <div className="flex items-center gap-2 mb-2">
                  <FileText className="w-5 h-5 text-gray-500" />
                  <p className="text-sm font-medium text-gray-600">進捗投稿</p>
                </div>
                <p className="text-3xl font-bold text-gray-900">{stats.posts.total}</p>
                <p className="text-sm text-gray-500 mt-1">
                  今週: <span className="font-medium text-green-600">{stats.posts.recentWeek}</span>
                </p>
              </div>
              <div className="bg-green-50 rounded-full p-4">
                <FileText className="w-8 h-8 text-green-600" />
              </div>
            </div>
          </div>

          {/* メッセージ統計 */}
          <div className="bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition-shadow">
            <div className="flex items-center justify-between">
              <div>
                <div className="flex items-center gap-2 mb-2">
                  <MessageSquare className="w-5 h-5 text-gray-500" />
                  <p className="text-sm font-medium text-gray-600">メッセージ</p>
                </div>
                <p className="text-3xl font-bold text-gray-900">{stats.messages.total}</p>
              </div>
              <div className="bg-purple-50 rounded-full p-4">
                <MessageSquare className="w-8 h-8 text-purple-600" />
              </div>
            </div>
          </div>

          {/* OKR統計 */}
          <div className="bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition-shadow">
            <div className="flex items-center justify-between">
              <div className="w-full">
                <div className="flex items-center gap-2 mb-2">
                  <Target className="w-5 h-5 text-gray-500" />
                  <p className="text-sm font-medium text-gray-600">OKR</p>
                </div>
                <p className="text-3xl font-bold text-gray-900">{stats.okrs.total}</p>
                <div className="mt-3">
                  <div className="flex justify-between text-sm mb-1">
                    <span className="text-gray-500">完了率</span>
                    <span className="font-medium text-gray-900">{stats.okrs.completionRate.toFixed(1)}%</span>
                  </div>
                  <div className="w-full bg-gray-200 rounded-full h-2.5">
                    <div
                      className="bg-gradient-to-r from-blue-500 to-blue-600 h-2.5 rounded-full transition-all duration-500"
                      style={{ width: `${stats.okrs.completionRate}%` }}
                    ></div>
                  </div>
                  <div className="flex justify-between text-xs text-gray-500 mt-2">
                    <span>進行中: {stats.okrs.inProgress}</span>
                    <span>完了: {stats.okrs.completed}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* 通報統計 */}
          <div className="bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition-shadow">
            <div className="flex items-center justify-between">
              <div>
                <div className="flex items-center gap-2 mb-2">
                  <AlertTriangle className="w-5 h-5 text-gray-500" />
                  <p className="text-sm font-medium text-gray-600">保留中の通報</p>
                </div>
                <p className="text-3xl font-bold text-red-600">{stats.reports.pending}</p>
                {stats.reports.pending > 0 && (
                  <button
                    onClick={() => router.push('/admin/reports')}
                    className="mt-2 text-sm text-blue-600 hover:text-blue-800 flex items-center gap-1"
                  >
                    詳細を見る <span>→</span>
                  </button>
                )}
              </div>
              <div className="bg-red-50 rounded-full p-4">
                <AlertTriangle className="w-8 h-8 text-red-600" />
              </div>
            </div>
          </div>

          {/* グループ統計 */}
          <div className="bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition-shadow">
            <div className="flex items-center justify-between">
              <div>
                <div className="flex items-center gap-2 mb-2">
                  <Layers className="w-5 h-5 text-gray-500" />
                  <p className="text-sm font-medium text-gray-600">グループ</p>
                </div>
                <p className="text-3xl font-bold text-gray-900">{stats.groups.total}</p>
              </div>
              <div className="bg-indigo-50 rounded-full p-4">
                <Layers className="w-8 h-8 text-indigo-600" />
              </div>
            </div>
          </div>
        </div>

        {/* アクティビティグラフ */}
        <div className="mt-8 bg-white rounded-lg shadow-md p-6">
          <div className="flex items-center justify-between mb-6">
            <div className="flex items-center gap-2">
              <Activity className="w-5 h-5 text-gray-600" />
              <h2 className="text-lg font-semibold text-gray-900">アクティビティ推移</h2>
            </div>
            <div className="flex gap-2">
              <button
                onClick={() => setTimeRange('7d')}
                className={`px-3 py-1 text-sm rounded ${
                  timeRange === '7d'
                    ? 'bg-blue-600 text-white'
                    : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                }`}
              >
                7日間
              </button>
              <button
                onClick={() => setTimeRange('30d')}
                className={`px-3 py-1 text-sm rounded ${
                  timeRange === '30d'
                    ? 'bg-blue-600 text-white'
                    : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                }`}
              >
                30日間
              </button>
              <button
                onClick={() => setTimeRange('90d')}
                className={`px-3 py-1 text-sm rounded ${
                  timeRange === '90d'
                    ? 'bg-blue-600 text-white'
                    : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                }`}
              >
                90日間
              </button>
            </div>
          </div>

          {activityData.length > 0 ? (
            <div className="mt-4">
              <div className="flex items-end justify-between h-64 gap-2">
                {activityData.map((data, index) => {
                  const maxValue = Math.max(...activityData.map(d => Math.max(d.posts, d.messages)));
                  const postsHeight = (data.posts / maxValue) * 100;
                  const messagesHeight = (data.messages / maxValue) * 100;

                  return (
                    <div key={index} className="flex-1 flex flex-col items-center gap-1">
                      <div className="w-full flex gap-1 items-end h-48">
                        <div
                          className="flex-1 bg-green-500 rounded-t hover:bg-green-600 transition-colors"
                          style={{ height: `${postsHeight}%` }}
                          title={`進捗投稿: ${data.posts}`}
                        ></div>
                        <div
                          className="flex-1 bg-purple-500 rounded-t hover:bg-purple-600 transition-colors"
                          style={{ height: `${messagesHeight}%` }}
                          title={`メッセージ: ${data.messages}`}
                        ></div>
                      </div>
                      <span className="text-xs text-gray-500">
                        {new Date(data.date).toLocaleDateString('ja-JP', { month: 'short', day: 'numeric' })}
                      </span>
                    </div>
                  );
                })}
              </div>
              <div className="flex items-center justify-center gap-6 mt-4">
                <div className="flex items-center gap-2">
                  <div className="w-3 h-3 bg-green-500 rounded"></div>
                  <span className="text-sm text-gray-600">進捗投稿</span>
                </div>
                <div className="flex items-center gap-2">
                  <div className="w-3 h-3 bg-purple-500 rounded"></div>
                  <span className="text-sm text-gray-600">メッセージ</span>
                </div>
              </div>
            </div>
          ) : (
            <div className="text-center py-8 text-gray-500">
              データを読み込んでいます...
            </div>
          )}
        </div>

        {/* アクションボタン */}
        <div className="mt-8 bg-white rounded-lg shadow-md p-6">
          <div className="flex items-center gap-2 mb-4">
            <BarChart3 className="w-5 h-5 text-gray-600" />
            <h2 className="text-lg font-semibold text-gray-900">クイックアクション</h2>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <button
              onClick={() => router.push('/admin/users')}
              className="p-4 border-2 border-gray-200 rounded-lg hover:border-blue-400 hover:bg-blue-50 text-left transition-all group"
            >
              <div className="flex items-center gap-3 mb-2">
                <Users className="w-5 h-5 text-blue-600" />
                <h3 className="font-medium text-gray-900 group-hover:text-blue-700">ユーザー管理</h3>
              </div>
              <p className="text-sm text-gray-500">ユーザーの閲覧・編集</p>
            </button>
            <button
              onClick={() => router.push('/admin/reports')}
              className="p-4 border-2 border-gray-200 rounded-lg hover:border-red-400 hover:bg-red-50 text-left transition-all group"
            >
              <div className="flex items-center gap-3 mb-2">
                <AlertTriangle className="w-5 h-5 text-red-600" />
                <h3 className="font-medium text-gray-900 group-hover:text-red-700">通報管理</h3>
              </div>
              <p className="text-sm text-gray-500">通報の確認・対応</p>
            </button>
            <button
              onClick={() => router.push('/admin/action-logs')}
              className="p-4 border-2 border-gray-200 rounded-lg hover:border-indigo-400 hover:bg-indigo-50 text-left transition-all group"
            >
              <div className="flex items-center gap-3 mb-2">
                <Activity className="w-5 h-5 text-indigo-600" />
                <h3 className="font-medium text-gray-900 group-hover:text-indigo-700">アクションログ</h3>
              </div>
              <p className="text-sm text-gray-500">管理者操作の履歴</p>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
