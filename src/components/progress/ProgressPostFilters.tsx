'use client';

import React from 'react';

interface Filters {
  tenantId: number;
  organizationId: number | null;
  postType: string | null;
  startDate: string | null;
  endDate: string | null;
  page: number;
  size: number;
}

interface ProgressPostFiltersProps {
  filters: Filters;
  onFilterChange: (newFilters: Partial<Filters>) => void;
}

export default function ProgressPostFilters({ filters, onFilterChange }: ProgressPostFiltersProps) {
  const handlePostTypeChange = (postType: string) => {
    onFilterChange({
      postType: postType === filters.postType ? null : postType
    });
  };

  const handleDateRangeChange = (type: 'today' | 'week' | 'month' | 'clear') => {
    const today = new Date();
    let startDate: string | null = null;
    let endDate: string | null = null;

    switch (type) {
      case 'today':
        startDate = today.toISOString().split('T')[0];
        endDate = startDate;
        break;
      case 'week':
        const weekAgo = new Date(today);
        weekAgo.setDate(weekAgo.getDate() - 7);
        startDate = weekAgo.toISOString().split('T')[0];
        endDate = today.toISOString().split('T')[0];
        break;
      case 'month':
        const monthAgo = new Date(today);
        monthAgo.setMonth(monthAgo.getMonth() - 1);
        startDate = monthAgo.toISOString().split('T')[0];
        endDate = today.toISOString().split('T')[0];
        break;
      case 'clear':
        startDate = null;
        endDate = null;
        break;
    }

    onFilterChange({ startDate, endDate });
  };

  return (
    <div className="bg-white rounded-lg shadow p-6 space-y-6">
      <h2 className="text-lg font-semibold text-gray-900">フィルター</h2>

      {/* 投稿タイプ */}
      <div>
        <h3 className="text-sm font-medium text-gray-700 mb-3">投稿タイプ</h3>
        <div className="space-y-2">
          {[
            { value: 'PROGRESS', label: '進捗報告', icon: '📊' },
            { value: 'GOAL', label: '目標設定', icon: '🎯' },
            { value: 'CHALLENGE', label: '課題', icon: '🚧' },
            { value: 'ACHIEVEMENT', label: '達成', icon: '🎉' },
            { value: 'LEARNING', label: '学び', icon: '💡' }
          ].map(({ value, label, icon }) => (
            <button
              key={value}
              onClick={() => handlePostTypeChange(value)}
              className={`w-full text-left px-3 py-2 rounded-lg transition-colors ${
                filters.postType === value
                  ? 'bg-blue-100 text-blue-800 font-medium'
                  : 'text-gray-700 hover:bg-gray-100'
              }`}
            >
              <span className="mr-2">{icon}</span>
              {label}
            </button>
          ))}
        </div>
      </div>

      {/* 期間 */}
      <div>
        <h3 className="text-sm font-medium text-gray-700 mb-3">期間</h3>
        <div className="space-y-2">
          <button
            onClick={() => handleDateRangeChange('today')}
            className="w-full text-left px-3 py-2 rounded-lg text-gray-700 hover:bg-gray-100 transition-colors"
          >
            今日
          </button>
          <button
            onClick={() => handleDateRangeChange('week')}
            className="w-full text-left px-3 py-2 rounded-lg text-gray-700 hover:bg-gray-100 transition-colors"
          >
            過去1週間
          </button>
          <button
            onClick={() => handleDateRangeChange('month')}
            className="w-full text-left px-3 py-2 rounded-lg text-gray-700 hover:bg-gray-100 transition-colors"
          >
            過去1ヶ月
          </button>
          {(filters.startDate || filters.endDate) && (
            <button
              onClick={() => handleDateRangeChange('clear')}
              className="w-full text-left px-3 py-2 rounded-lg text-red-600 hover:bg-red-50 transition-colors"
            >
              期間をクリア
            </button>
          )}
        </div>
      </div>

      {/* カスタム期間 */}
      <div>
        <h3 className="text-sm font-medium text-gray-700 mb-3">カスタム期間</h3>
        <div className="space-y-2">
          <div>
            <label className="block text-xs text-gray-600 mb-1">開始日</label>
            <input
              type="date"
              value={filters.startDate || ''}
              onChange={(e) => onFilterChange({ startDate: e.target.value || null })}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>
          <div>
            <label className="block text-xs text-gray-600 mb-1">終了日</label>
            <input
              type="date"
              value={filters.endDate || ''}
              onChange={(e) => onFilterChange({ endDate: e.target.value || null })}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>
        </div>
      </div>

      {/* フィルタークリア */}
      <button
        onClick={() => onFilterChange({
          postType: null,
          startDate: null,
          endDate: null,
          organizationId: null
        })}
        className="w-full px-4 py-2 text-sm text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors"
      >
        すべてのフィルターをクリア
      </button>

      {/* 現在のフィルター表示 */}
      {(filters.postType || filters.startDate || filters.endDate) && (
        <div className="pt-4 border-t border-gray-200">
          <h3 className="text-xs font-medium text-gray-600 mb-2">適用中のフィルター</h3>
          <div className="space-y-1 text-xs text-gray-700">
            {filters.postType && (
              <div className="flex items-center justify-between">
                <span>タイプ:</span>
                <span className="font-medium">{filters.postType}</span>
              </div>
            )}
            {filters.startDate && (
              <div className="flex items-center justify-between">
                <span>開始:</span>
                <span className="font-medium">{filters.startDate}</span>
              </div>
            )}
            {filters.endDate && (
              <div className="flex items-center justify-between">
                <span>終了:</span>
                <span className="font-medium">{filters.endDate}</span>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
