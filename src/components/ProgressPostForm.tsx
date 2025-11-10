'use client';

import { useState } from 'react';
import { progressPostService, CreateProgressPostRequest, PostType, Visibility } from '@/services/api';

interface ProgressPostFormProps {
  tenantId: number;
  organizationId: number;
  authorId: number;
  onPostCreated?: () => void;
  onCancel?: () => void;
}

export default function ProgressPostForm({
  tenantId,
  organizationId,
  authorId,
  onPostCreated,
  onCancel
}: ProgressPostFormProps) {
  const [formData, setFormData] = useState<CreateProgressPostRequest>({
    tenantId,
    organizationId,
    authorId,
    postType: 'PROGRESS',
    content: '',
    postDate: new Date().toISOString().split('T')[0],
    visibility: 'ORGANIZATION',
    isAnonymous: true,
  });

  const [tags, setTags] = useState<string>('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!formData.content.trim()) {
      setError('内容を入力してください');
      return;
    }

    try {
      setLoading(true);
      setError(null);

      const postData: CreateProgressPostRequest = {
        ...formData,
        tags: tags
          .split(',')
          .map(tag => tag.trim())
          .filter(tag => tag.length > 0),
      };

      await progressPostService.createPost(postData);

      setSuccess(true);
      setTimeout(() => {
        setSuccess(false);
        if (onPostCreated) {
          onPostCreated();
        }
        // フォームをリセット
        setFormData({
          tenantId,
          organizationId,
          authorId,
          postType: 'PROGRESS',
          content: '',
          postDate: new Date().toISOString().split('T')[0],
          visibility: 'ORGANIZATION',
          isAnonymous: true,
        });
        setTags('');
      }, 1500);
    } catch (err: any) {
      console.error('投稿エラー:', err);
      setError(err.response?.data?.message || '投稿の作成に失敗しました');
    } finally {
      setLoading(false);
    }
  };

  const postTypes: { value: PostType; label: string; icon: string }[] = [
    { value: 'PROGRESS', label: '進捗', icon: '📈' },
    { value: 'GOAL', label: '目標', icon: '🎯' },
    { value: 'BLOCKER', label: '障害', icon: '🚧' },
    { value: 'LEARNING', label: '学び', icon: '💡' },
    { value: 'QUESTION', label: '質問', icon: '❓' },
  ];

  const visibilityOptions: { value: Visibility; label: string; icon: string }[] = [
    { value: 'TENANT', label: '全社', icon: '🏢' },
    { value: 'ORGANIZATION', label: '部門', icon: '🏛️' },
    { value: 'TEAM', label: 'チーム', icon: '👥' },
    { value: 'PRIVATE', label: '非公開', icon: '🔒' },
  ];

  if (success) {
    return (
      <div className="rounded-lg p-8 text-center" style={{ backgroundColor: '#393E46' }}>
        <div className="text-6xl mb-4">✅</div>
        <h3 style={{ color: '#00ADB5' }} className="text-2xl font-bold mb-2">
          投稿が完了しました！
        </h3>
        <p style={{ color: '#EEEEEE' }}>タイムラインに反映されます</p>
      </div>
    );
  }

  return (
    <div className="rounded-lg p-6" style={{ backgroundColor: '#393E46' }}>
      <h2 className="text-2xl font-bold mb-6" style={{ color: '#EEEEEE' }}>
        📝 新しい投稿を作成
      </h2>

      {error && (
        <div className="mb-4 p-4 rounded" style={{ backgroundColor: '#FF6B6B20', borderLeft: '4px solid #FF6B6B' }}>
          <p style={{ color: '#FF6B6B' }}>❌ {error}</p>
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* 投稿タイプ */}
        <div>
          <label className="block mb-2 font-semibold" style={{ color: '#EEEEEE' }}>
            投稿タイプ
          </label>
          <div className="grid grid-cols-5 gap-2">
            {postTypes.map((type) => (
              <button
                key={type.value}
                type="button"
                onClick={() => setFormData({ ...formData, postType: type.value })}
                className="px-4 py-3 rounded-lg font-semibold transition-all"
                style={{
                  backgroundColor: formData.postType === type.value ? '#00ADB5' : '#222831',
                  color: '#EEEEEE'
                }}
              >
                <div className="text-2xl mb-1">{type.icon}</div>
                <div className="text-xs">{type.label}</div>
              </button>
            ))}
          </div>
        </div>

        {/* タイトル（オプション） */}
        <div>
          <label className="block mb-2 font-semibold" style={{ color: '#EEEEEE' }}>
            タイトル（任意）
          </label>
          <input
            type="text"
            value={formData.title || ''}
            onChange={(e) => setFormData({ ...formData, title: e.target.value })}
            placeholder="タイトルを入力..."
            className="w-full px-4 py-3 rounded-lg"
            style={{ backgroundColor: '#222831', color: '#EEEEEE', border: '2px solid transparent' }}
            onFocus={(e) => e.currentTarget.style.borderColor = '#00ADB5'}
            onBlur={(e) => e.currentTarget.style.borderColor = 'transparent'}
          />
        </div>

        {/* 内容 */}
        <div>
          <label className="block mb-2 font-semibold" style={{ color: '#EEEEEE' }}>
            内容 <span style={{ color: '#FF6B6B' }}>*</span>
          </label>
          <textarea
            value={formData.content}
            onChange={(e) => setFormData({ ...formData, content: e.target.value })}
            placeholder="今日の進捗や目標を入力..."
            rows={6}
            required
            className="w-full px-4 py-3 rounded-lg"
            style={{ backgroundColor: '#222831', color: '#EEEEEE', border: '2px solid transparent' }}
            onFocus={(e) => e.currentTarget.style.borderColor = '#00ADB5'}
            onBlur={(e) => e.currentTarget.style.borderColor = 'transparent'}
          />
        </div>

        {/* 達成率（進捗タイプの場合） */}
        {formData.postType === 'PROGRESS' && (
          <div>
            <label className="block mb-2 font-semibold" style={{ color: '#EEEEEE' }}>
              達成率: {formData.achievementRate || 0}%
            </label>
            <input
              type="range"
              min="0"
              max="100"
              value={formData.achievementRate || 0}
              onChange={(e) => setFormData({ ...formData, achievementRate: parseInt(e.target.value) })}
              className="w-full"
              style={{ accentColor: '#00ADB5' }}
            />
          </div>
        )}

        {/* ブロッカー */}
        <div>
          <label className="block mb-2 font-semibold" style={{ color: '#EEEEEE' }}>
            🚧 ブロッカー・障害（任意）
          </label>
          <textarea
            value={formData.blockers || ''}
            onChange={(e) => setFormData({ ...formData, blockers: e.target.value })}
            placeholder="進捗を妨げている課題や障害..."
            rows={3}
            className="w-full px-4 py-3 rounded-lg"
            style={{ backgroundColor: '#222831', color: '#EEEEEE', border: '2px solid transparent' }}
            onFocus={(e) => e.currentTarget.style.borderColor = '#00ADB5'}
            onBlur={(e) => e.currentTarget.style.borderColor = 'transparent'}
          />
        </div>

        {/* 学び */}
        <div>
          <label className="block mb-2 font-semibold" style={{ color: '#EEEEEE' }}>
            💡 学び・気づき（任意）
          </label>
          <textarea
            value={formData.learnings || ''}
            onChange={(e) => setFormData({ ...formData, learnings: e.target.value })}
            placeholder="今日学んだことや気づき..."
            rows={3}
            className="w-full px-4 py-3 rounded-lg"
            style={{ backgroundColor: '#222831', color: '#EEEEEE', border: '2px solid transparent' }}
            onFocus={(e) => e.currentTarget.style.borderColor = '#00ADB5'}
            onBlur={(e) => e.currentTarget.style.borderColor = 'transparent'}
          />
        </div>

        {/* 次のアクション */}
        <div>
          <label className="block mb-2 font-semibold" style={{ color: '#EEEEEE' }}>
            🎯 次のアクション（任意）
          </label>
          <textarea
            value={formData.nextAction || ''}
            onChange={(e) => setFormData({ ...formData, nextAction: e.target.value })}
            placeholder="次に取り組むこと..."
            rows={2}
            className="w-full px-4 py-3 rounded-lg"
            style={{ backgroundColor: '#222831', color: '#EEEEEE', border: '2px solid transparent' }}
            onFocus={(e) => e.currentTarget.style.borderColor = '#00ADB5'}
            onBlur={(e) => e.currentTarget.style.borderColor = 'transparent'}
          />
        </div>

        {/* タグ */}
        <div>
          <label className="block mb-2 font-semibold" style={{ color: '#EEEEEE' }}>
            🏷️ タグ（カンマ区切り、任意）
          </label>
          <input
            type="text"
            value={tags}
            onChange={(e) => setTags(e.target.value)}
            placeholder="例: React, バックエンド, 設計"
            className="w-full px-4 py-3 rounded-lg"
            style={{ backgroundColor: '#222831', color: '#EEEEEE', border: '2px solid transparent' }}
            onFocus={(e) => e.currentTarget.style.borderColor = '#00ADB5'}
            onBlur={(e) => e.currentTarget.style.borderColor = 'transparent'}
          />
        </div>

        {/* 公開範囲 */}
        <div>
          <label className="block mb-2 font-semibold" style={{ color: '#EEEEEE' }}>
            公開範囲
          </label>
          <div className="grid grid-cols-4 gap-2">
            {visibilityOptions.map((option) => (
              <button
                key={option.value}
                type="button"
                onClick={() => setFormData({ ...formData, visibility: option.value })}
                className="px-4 py-3 rounded-lg font-semibold transition-all"
                style={{
                  backgroundColor: formData.visibility === option.value ? '#00ADB5' : '#222831',
                  color: '#EEEEEE'
                }}
              >
                <div className="text-xl mb-1">{option.icon}</div>
                <div className="text-xs">{option.label}</div>
              </button>
            ))}
          </div>
        </div>

        {/* 匿名性 */}
        <div className="flex items-center gap-3">
          <input
            type="checkbox"
            id="isAnonymous"
            checked={formData.isAnonymous}
            onChange={(e) => setFormData({ ...formData, isAnonymous: e.target.checked })}
            className="w-5 h-5"
            style={{ accentColor: '#00ADB5' }}
          />
          <label htmlFor="isAnonymous" style={{ color: '#EEEEEE' }} className="cursor-pointer">
            🎭 匿名で投稿する
          </label>
        </div>

        {/* 送信ボタン */}
        <div className="flex gap-4 pt-4">
          {onCancel && (
            <button
              type="button"
              onClick={onCancel}
              className="px-6 py-3 rounded-lg font-semibold transition-opacity flex-1"
              style={{ backgroundColor: '#222831', color: '#EEEEEE' }}
              onMouseEnter={(e) => e.currentTarget.style.opacity = '0.8'}
              onMouseLeave={(e) => e.currentTarget.style.opacity = '1'}
            >
              キャンセル
            </button>
          )}
          <button
            type="submit"
            disabled={loading || !formData.content.trim()}
            className="px-6 py-3 rounded-lg font-semibold transition-opacity flex-1"
            style={{
              backgroundColor: '#00ADB5',
              color: '#EEEEEE',
              opacity: loading || !formData.content.trim() ? 0.5 : 1
            }}
            onMouseEnter={(e) => {
              if (!loading && formData.content.trim()) {
                e.currentTarget.style.opacity = '0.8';
              }
            }}
            onMouseLeave={(e) => {
              if (!loading && formData.content.trim()) {
                e.currentTarget.style.opacity = '1';
              }
            }}
          >
            {loading ? '投稿中...' : '📤 投稿する'}
          </button>
        </div>
      </form>
    </div>
  );
}
