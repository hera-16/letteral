'use client';

import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import {
  AlertTriangle,
  CheckCircle,
  XCircle,
  Clock,
  Eye,
  User,
  Calendar,
  FileText,
  Search,
  Filter
} from 'lucide-react';

interface Report {
  id: number;
  reportType: string;
  reason: string;
  status: 'PENDING' | 'REVIEWING' | 'RESOLVED' | 'DISMISSED';
  reporter: {
    id: number;
    username: string;
  };
  reportedUser?: {
    id: number;
    username: string;
  };
  createdAt: string;
  resolvedAt?: string;
  resolution?: string;
}

const REPORT_TYPE_LABELS: Record<string, string> = {
  HARASSMENT: 'ハラスメント',
  INAPPROPRIATE_CONTENT: '不適切なコンテンツ',
  SPAM: 'スパム',
  FALSE_INFORMATION: '虚偽の情報',
  PRIVACY_VIOLATION: 'プライバシー侵害',
  OTHER: 'その他',
};

const STATUS_LABELS: Record<string, string> = {
  PENDING: '保留中',
  REVIEWING: 'レビュー中',
  RESOLVED: '解決済み',
  DISMISSED: '却下',
};

const STATUS_COLORS: Record<string, string> = {
  PENDING: 'bg-yellow-100 text-yellow-800',
  REVIEWING: 'bg-blue-100 text-blue-800',
  RESOLVED: 'bg-green-100 text-green-800',
  DISMISSED: 'bg-gray-100 text-gray-800',
};

export default function ReportsPage() {
  const router = useRouter();
  const [reports, setReports] = useState<Report[]>([]);
  const [selectedReport, setSelectedReport] = useState<Report | null>(null);
  const [filterStatus, setFilterStatus] = useState<string>('ALL');
  const [filterType, setFilterType] = useState<string>('ALL');
  const [searchQuery, setSearchQuery] = useState('');
  const [loading, setLoading] = useState(true);
  const [resolving, setResolving] = useState(false);
  const [resolution, setResolution] = useState('');
  const [resolveStatus, setResolveStatus] = useState<'RESOLVED' | 'DISMISSED'>('RESOLVED');

  useEffect(() => {
    fetchReports();
  }, [filterStatus]);

  const fetchReports = async () => {
    try {
      setLoading(true);
      const url = filterStatus === 'ALL'
        ? '/api/reports?page=0&size=100'
        : `/api/reports/status/${filterStatus}?page=0&size=100`;

      const response = await fetch(url, {
        credentials: 'include',
      });

      if (!response.ok) throw new Error('Failed to fetch reports');

      const data = await response.json();
      setReports(data.content || data);
    } catch (error) {
      console.error('Error fetching reports:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleResolveReport = async () => {
    if (!selectedReport || !resolution.trim()) return;

    try {
      setResolving(true);
      const response = await fetch(`/api/reports/${selectedReport.id}/resolve`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({
          status: resolveStatus,
          resolution: resolution,
        }),
      });

      if (!response.ok) throw new Error('Failed to resolve report');

      setSelectedReport(null);
      setResolution('');
      fetchReports();
    } catch (error) {
      console.error('Error resolving report:', error);
      alert('通報の処理に失敗しました');
    } finally {
      setResolving(false);
    }
  };

  // フィルター適用
  const filteredReports = reports.filter(report => {
    // ステータスフィルター
    if (filterStatus !== 'ALL' && report.status !== filterStatus) {
      return false;
    }

    // タイプフィルター
    if (filterType !== 'ALL' && report.reportType !== filterType) {
      return false;
    }

    // 検索クエリ
    if (searchQuery.trim()) {
      const query = searchQuery.toLowerCase();
      return (
        report.reason.toLowerCase().includes(query) ||
        report.reporter.username.toLowerCase().includes(query) ||
        report.reportedUser?.username.toLowerCase().includes(query)
      );
    }

    return true;
  });

  // 統計情報
  const stats = {
    pending: reports.filter(r => r.status === 'PENDING').length,
    reviewing: reports.filter(r => r.status === 'REVIEWING').length,
    resolved: reports.filter(r => r.status === 'RESOLVED').length,
    dismissed: reports.filter(r => r.status === 'DISMISSED').length,
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* ヘッダー */}
      <div className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="flex justify-between items-center">
            <h1 className="text-2xl font-bold text-gray-900">通報管理</h1>
            <button
              onClick={() => router.push('/admin/dashboard')}
              className="px-4 py-2 text-sm bg-gray-100 text-gray-700 rounded hover:bg-gray-200"
            >
              ダッシュボードに戻る
            </button>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* 統計カード */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
          <div className="bg-white rounded-lg shadow-md p-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">保留中</p>
                <p className="text-2xl font-bold text-yellow-600">{stats.pending}</p>
              </div>
              <Clock className="w-8 h-8 text-yellow-600" />
            </div>
          </div>
          <div className="bg-white rounded-lg shadow-md p-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">レビュー中</p>
                <p className="text-2xl font-bold text-blue-600">{stats.reviewing}</p>
              </div>
              <Eye className="w-8 h-8 text-blue-600" />
            </div>
          </div>
          <div className="bg-white rounded-lg shadow-md p-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">解決済み</p>
                <p className="text-2xl font-bold text-green-600">{stats.resolved}</p>
              </div>
              <CheckCircle className="w-8 h-8 text-green-600" />
            </div>
          </div>
          <div className="bg-white rounded-lg shadow-md p-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">却下</p>
                <p className="text-2xl font-bold text-gray-600">{stats.dismissed}</p>
              </div>
              <XCircle className="w-8 h-8 text-gray-600" />
            </div>
          </div>
        </div>

        {/* 検索とフィルター */}
        <div className="bg-white rounded-lg shadow-md p-6 mb-6">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {/* 検索 */}
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
              <input
                type="text"
                placeholder="検索（理由、ユーザー名）"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>

            {/* ステータスフィルター */}
            <div className="relative">
              <Filter className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
              <select
                value={filterStatus}
                onChange={(e) => setFilterStatus(e.target.value)}
                className="w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="ALL">すべてのステータス</option>
                {Object.entries(STATUS_LABELS).map(([status, label]) => (
                  <option key={status} value={status}>{label}</option>
                ))}
              </select>
            </div>

            {/* タイプフィルター */}
            <div className="relative">
              <AlertTriangle className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
              <select
                value={filterType}
                onChange={(e) => setFilterType(e.target.value)}
                className="w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="ALL">すべてのタイプ</option>
                {Object.entries(REPORT_TYPE_LABELS).map(([type, label]) => (
                  <option key={type} value={type}>{label}</option>
                ))}
              </select>
            </div>
          </div>

          <div className="mt-3 text-sm text-gray-600">
            {filteredReports.length} 件の通報を表示
            {(searchQuery || filterStatus !== 'ALL' || filterType !== 'ALL') && ' （フィルター適用中）'}
          </div>
        </div>

        {/* 通報リスト */}
        <div className="bg-white rounded-lg shadow-md">
          {loading ? (
            <div className="p-8 text-center">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
              <p className="mt-2 text-gray-600">読み込み中...</p>
            </div>
          ) : filteredReports.length === 0 ? (
            <div className="p-8 text-center text-gray-500">
              {searchQuery || filterStatus !== 'ALL' || filterType !== 'ALL'
                ? '条件に一致する通報はありません'
                : '通報はありません'
              }
            </div>
          ) : (
            <div className="divide-y divide-gray-200">
              {filteredReports.map((report) => (
                <div
                  key={report.id}
                  className="p-6 hover:bg-gray-50 cursor-pointer transition-colors"
                  onClick={() => setSelectedReport(report)}
                >
                  <div className="flex justify-between items-start">
                    <div className="flex-1">
                      <div className="flex items-center gap-3 mb-3">
                        <span className={`px-3 py-1 rounded-full text-xs font-medium ${STATUS_COLORS[report.status]}`}>
                          {STATUS_LABELS[report.status]}
                        </span>
                        <span className="px-3 py-1 rounded-full text-xs font-medium bg-gray-100 text-gray-800">
                          {REPORT_TYPE_LABELS[report.reportType]}
                        </span>
                      </div>
                      <p className="text-sm text-gray-900 mb-3 line-clamp-2">{report.reason}</p>
                      <div className="flex items-center gap-4 text-xs text-gray-500">
                        <div className="flex items-center gap-1">
                          <User className="w-3 h-3" />
                          <span>報告者: {report.reporter.username}</span>
                        </div>
                        {report.reportedUser && (
                          <div className="flex items-center gap-1">
                            <AlertTriangle className="w-3 h-3" />
                            <span>対象: {report.reportedUser.username}</span>
                          </div>
                        )}
                        <div className="flex items-center gap-1">
                          <Calendar className="w-3 h-3" />
                          <span>{new Date(report.createdAt).toLocaleString('ja-JP')}</span>
                        </div>
                      </div>
                    </div>
                    {report.status === 'PENDING' && (
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          setSelectedReport(report);
                        }}
                        className="ml-4 px-4 py-2 bg-blue-600 text-white text-sm rounded-lg hover:bg-blue-700 flex items-center gap-2 transition-colors"
                      >
                        <Eye className="w-4 h-4" />
                        対応する
                      </button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* 対応モーダル */}
      {selectedReport && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-lg max-w-2xl w-full max-h-[90vh] overflow-y-auto">
            <div className="p-6">
              <div className="flex justify-between items-start mb-4">
                <h2 className="text-xl font-bold text-gray-900">通報の詳細</h2>
                <button
                  onClick={() => setSelectedReport(null)}
                  className="text-gray-400 hover:text-gray-600"
                >
                  <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>
              </div>

              <div className="space-y-4">
                <div>
                  <label className="text-sm font-medium text-gray-700">種類</label>
                  <p className="mt-1">{REPORT_TYPE_LABELS[selectedReport.reportType]}</p>
                </div>

                <div>
                  <label className="text-sm font-medium text-gray-700">理由</label>
                  <p className="mt-1 text-gray-900">{selectedReport.reason}</p>
                </div>

                <div>
                  <label className="text-sm font-medium text-gray-700">報告者</label>
                  <p className="mt-1">{selectedReport.reporter.username}</p>
                </div>

                {selectedReport.reportedUser && (
                  <div>
                    <label className="text-sm font-medium text-gray-700">報告対象ユーザー</label>
                    <p className="mt-1">{selectedReport.reportedUser.username}</p>
                  </div>
                )}

                <div>
                  <label className="text-sm font-medium text-gray-700">報告日時</label>
                  <p className="mt-1">{new Date(selectedReport.createdAt).toLocaleString('ja-JP')}</p>
                </div>

                {selectedReport.status === 'PENDING' && (
                  <>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        対応結果
                      </label>
                      <select
                        value={resolveStatus}
                        onChange={(e) => setResolveStatus(e.target.value as 'RESOLVED' | 'DISMISSED')}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                      >
                        <option value="RESOLVED">解決済み</option>
                        <option value="DISMISSED">却下</option>
                      </select>
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        対応内容・コメント
                      </label>
                      <textarea
                        value={resolution}
                        onChange={(e) => setResolution(e.target.value)}
                        rows={4}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                        placeholder="対応した内容を記入してください"
                      />
                    </div>

                    <div className="flex gap-3 pt-4">
                      <button
                        onClick={handleResolveReport}
                        disabled={resolving || !resolution.trim()}
                        className="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-300 disabled:cursor-not-allowed"
                      >
                        {resolving ? '処理中...' : '対応を完了'}
                      </button>
                      <button
                        onClick={() => setSelectedReport(null)}
                        className="px-4 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200"
                      >
                        キャンセル
                      </button>
                    </div>
                  </>
                )}

                {selectedReport.status !== 'PENDING' && selectedReport.resolution && (
                  <div className="mt-4 p-4 bg-gray-50 rounded-lg">
                    <label className="text-sm font-medium text-gray-700">対応内容</label>
                    <p className="mt-1 text-gray-900">{selectedReport.resolution}</p>
                    {selectedReport.resolvedAt && (
                      <p className="mt-2 text-xs text-gray-500">
                        対応日時: {new Date(selectedReport.resolvedAt).toLocaleString('ja-JP')}
                      </p>
                    )}
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
