'use client';

import React, { useState } from 'react';

interface ProgressPostFormProps {
  onSubmit: (data: any) => void;
  onCancel: () => void;
  submitting?: boolean;
  initialData?: any;
}

export default function ProgressPostForm({
  onSubmit,
  onCancel,
  submitting = false,
  initialData
}: ProgressPostFormProps) {
  const [formData, setFormData] = useState({
    tenantId: initialData?.tenantId || 1, // TODO: 実際のテナントIDを取得
    organizationId: initialData?.organizationId || 1, // TODO: 実際の組織IDを取得
    postType: initialData?.postType || 'PROGRESS',
    title: initialData?.title || '',
    content: initialData?.content || '',
    achievementRate: initialData?.achievementRate || 0,
    blockers: initialData?.blockers || '',
    learnings: initialData?.learnings || '',
    nextAction: initialData?.nextAction || '',
    visibility: initialData?.visibility || 'ORGANIZATION',
    postDate: initialData?.postDate || new Date().toISOString().split('T')[0],
    tags: initialData?.tags || [] as string[],
    targetOrganizationId: initialData?.targetOrganizationId || null
  });

  const [tagInput, setTagInput] = useState('');

  const handleChange = (field: string, value: any) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  };

  const handleAddTag = () => {
    if (tagInput.trim() && !formData.tags.includes(tagInput.trim())) {
      setFormData(prev => ({
        ...prev,
        tags: [...prev.tags, tagInput.trim()]
      }));
      setTagInput('');
    }
  };

  const handleRemoveTag = (tag: string) => {
    setFormData(prev => ({
      ...prev,
      tags: prev.tags.filter(t => t !== tag)
    }));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSubmit(formData);
  };

  return (
    <form onSubmit={handleSubmit} className="bg-white rounded-lg shadow p-6 space-y-6">
      {/* 投稿タイプ */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">
          投稿タイプ <span className="text-red-500">*</span>
        </label>
        <div className="grid grid-cols-2 md:grid-cols-5 gap-2">
          {[
            { value: 'PROGRESS', label: '進捗報告', icon: '📊' },
            { value: 'GOAL', label: '目標設定', icon: '🎯' },
            { value: 'CHALLENGE', label: '課題', icon: '🚧' },
            { value: 'ACHIEVEMENT', label: '達成', icon: '🎉' },
            { value: 'LEARNING', label: '学び', icon: '💡' }
          ].map(({ value, label, icon }) => (
            <button
              key={value}
              type="button"
              onClick={() => handleChange('postType', value)}
              className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                formData.postType === value
                  ? 'bg-blue-600 text-white'
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              }`}
            >
              <span className="mr-1">{icon}</span>
              {label}
            </button>
          ))}
        </div>
      </div>

      {/* タイトル */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">
          タイトル（任意）
        </label>
        <input
          type="text"
          value={formData.title}
          onChange={(e) => handleChange('title', e.target.value)}
          placeholder="例: ユーザー認証機能の実装完了"
          className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
        />
      </div>

      {/* メインコンテンツ */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">
          内容 <span className="text-red-500">*</span>
        </label>
        <textarea
          value={formData.content}
          onChange={(e) => handleChange('content', e.target.value)}
          placeholder="今日の進捗、達成したこと、学んだことを記入してください..."
          required
          rows={6}
          className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
        />
      </div>

      {/* 達成率 */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">
          達成率: {formData.achievementRate}%
        </label>
        <input
          type="range"
          min="0"
          max="100"
          step="5"
          value={formData.achievementRate}
          onChange={(e) => handleChange('achievementRate', parseInt(e.target.value))}
          className="w-full"
        />
        <div className="w-full bg-gray-200 rounded-full h-2 mt-2">
          <div
            className="bg-blue-600 h-2 rounded-full transition-all duration-300"
            style={{ width: `${formData.achievementRate}%` }}
          ></div>
        </div>
      </div>

      {/* ブロッカー */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">
          🚧 ブロッカー・課題
        </label>
        <textarea
          value={formData.blockers}
          onChange={(e) => handleChange('blockers', e.target.value)}
          placeholder="進捗を妨げている課題や問題があれば記入してください..."
          rows={3}
          className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
        />
      </div>

      {/* 学び */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">
          💡 学び・気づき
        </label>
        <textarea
          value={formData.learnings}
          onChange={(e) => handleChange('learnings', e.target.value)}
          placeholder="今日得た学びや気づきを記入してください..."
          rows={3}
          className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
        />
      </div>

      {/* 次のアクション */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">
          🎯 次のアクション
        </label>
        <textarea
          value={formData.nextAction}
          onChange={(e) => handleChange('nextAction', e.target.value)}
          placeholder="次に取り組むことを記入してください..."
          rows={3}
          className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
        />
      </div>

      {/* 公開範囲 */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">
          公開範囲 <span className="text-red-500">*</span>
        </label>
        <select
          value={formData.visibility}
          onChange={(e) => handleChange('visibility', e.target.value)}
          className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
        >
          <option value="PUBLIC">🌐 全体公開</option>
          <option value="ORGANIZATION">🏢 組織内</option>
          <option value="TEAM">👥 チーム内</option>
          <option value="PRIVATE">🔒 非公開</option>
        </select>
      </div>

      {/* 投稿日 */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">
          投稿日 <span className="text-red-500">*</span>
        </label>
        <input
          type="date"
          value={formData.postDate}
          onChange={(e) => handleChange('postDate', e.target.value)}
          required
          className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
        />
      </div>

      {/* タグ */}
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-2">
          タグ
        </label>
        <div className="flex space-x-2 mb-2">
          <input
            type="text"
            value={tagInput}
            onChange={(e) => setTagInput(e.target.value)}
            onKeyPress={(e) => e.key === 'Enter' && (e.preventDefault(), handleAddTag())}
            placeholder="タグを入力してEnter"
            className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          />
          <button
            type="button"
            onClick={handleAddTag}
            className="px-4 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition-colors"
          >
            追加
          </button>
        </div>
        {formData.tags.length > 0 && (
          <div className="flex flex-wrap gap-2">
            {formData.tags.map((tag, index) => (
              <span
                key={index}
                className="inline-flex items-center px-3 py-1 rounded-full text-sm bg-blue-100 text-blue-800"
              >
                #{tag}
                <button
                  type="button"
                  onClick={() => handleRemoveTag(tag)}
                  className="ml-2 text-blue-600 hover:text-blue-800"
                >
                  ×
                </button>
              </span>
            ))}
          </div>
        )}
      </div>

      {/* アクションボタン */}
      <div className="flex justify-end space-x-4 pt-6 border-t border-gray-200">
        <button
          type="button"
          onClick={onCancel}
          disabled={submitting}
          className="px-6 py-2 text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
        >
          キャンセル
        </button>
        <button
          type="submit"
          disabled={submitting || !formData.content}
          className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {submitting ? '投稿中...' : '投稿する'}
        </button>
      </div>
    </form>
  );
}
