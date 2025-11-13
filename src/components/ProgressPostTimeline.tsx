'use client';

import { useState, useEffect } from 'react';
import { progressPostService, ProgressPost, PagedProgressPosts } from '@/services/api';

interface ProgressPostTimelineProps {
  tenantId: number;
  organizationId?: number;
}

export default function ProgressPostTimeline({ tenantId, organizationId }: ProgressPostTimelineProps) {
  const [posts, setPosts] = useState<ProgressPost[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(false);
  const [totalElements, setTotalElements] = useState(0);

  useEffect(() => {
    loadPosts();
  }, [tenantId, organizationId, page]);

  const loadPosts = async () => {
    try {
      setLoading(true);
      setError(null);

      let response: PagedProgressPosts;
      if (organizationId) {
        response = await progressPostService.getOrganizationTimeline(organizationId, page, 10);
      } else {
        response = await progressPostService.getTenantTimeline(tenantId, page, 10);
      }

      if (page === 0) {
        setPosts(response.content);
      } else {
        setPosts(prev => [...prev, ...response.content]);
      }

      setHasMore(!response.last);
      setTotalElements(response.totalElements);
    } catch (err: any) {
      console.error('進捗投稿の読み込みエラー:', err);
      setError(err.response?.data?.message || '進捗投稿の読み込みに失敗しました');
    } finally {
      setLoading(false);
    }
  };

  const handleReaction = async (postId: number) => {
    try {
      await progressPostService.incrementReaction(postId);
      // リアクション数を更新
      setPosts(prev => prev.map(post =>
        post.id === postId
          ? { ...post, reactionCount: (post.reactionCount || 0) + 1 }
          : post
      ));
    } catch (err) {
      console.error('リアクションエラー:', err);
    }
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('ja-JP', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  const getPostTypeLabel = (type: string) => {
    const labels: Record<string, string> = {
      PROGRESS: '📈 進捗',
      GOAL: '🎯 目標',
      BLOCKER: '🚧 障害',
      LEARNING: '💡 学び',
      QUESTION: '❓ 質問',
    };
    return labels[type] || type;
  };

  const getVisibilityLabel = (visibility: string) => {
    const labels: Record<string, string> = {
      TENANT: '🏢 全社',
      ORGANIZATION: '🏛️ 部門',
      TEAM: '👥 チーム',
      PRIVATE: '🔒 非公開',
    };
    return labels[visibility] || visibility;
  };

  if (loading && page === 0) {
    return (
      <div className="flex items-center justify-center py-8">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2" style={{ borderColor: '#00ADB5' }}></div>
        <span className="ml-3" style={{ color: '#EEEEEE' }}>読み込み中...</span>
      </div>
    );
  }

  if (error) {
    return (
      <div className="rounded-lg p-4" style={{ backgroundColor: '#393E46' }}>
        <p style={{ color: '#FF6B6B' }}>❌ {error}</p>
      </div>
    );
  }

  if (posts.length === 0) {
    return (
      <div className="rounded-lg p-8 text-center" style={{ backgroundColor: '#393E46' }}>
        <p style={{ color: '#EEEEEE' }} className="text-lg mb-2">📝 まだ投稿がありません</p>
        <p style={{ color: '#00ADB5' }} className="text-sm">最初の進捗を投稿してみましょう！</p>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <div className="flex justify-between items-center mb-4">
        <h2 className="text-2xl font-bold" style={{ color: '#EEEEEE' }}>
          📋 進捗タイムライン
        </h2>
        <span style={{ color: '#00ADB5' }} className="text-sm">
          {totalElements} 件の投稿
        </span>
      </div>

      {posts.map((post) => (
        <div
          key={post.id}
          className="rounded-lg p-6 transition-all hover:shadow-lg"
          style={{ backgroundColor: '#393E46' }}
        >
          {/* ヘッダー */}
          <div className="flex justify-between items-start mb-4">
            <div className="flex items-center gap-3">
              <span className="text-lg">{getPostTypeLabel(post.postType)}</span>
              <span style={{ color: '#EEEEEE' }} className="font-semibold">
                匿名ユーザー
              </span>
              <span style={{ color: '#00ADB5' }} className="text-xs px-2 py-1 rounded">
                {getVisibilityLabel(post.visibility)}
              </span>
            </div>
            <span style={{ color: '#EEEEEE' }} className="text-sm opacity-60">
              {formatDate(post.postDate)}
            </span>
          </div>

          {/* タイトル */}
          {post.title && (
            <h3 style={{ color: '#EEEEEE' }} className="text-xl font-bold mb-3">
              {post.title}
            </h3>
          )}

          {/* コンテンツ */}
          <div style={{ color: '#EEEEEE' }} className="mb-4 whitespace-pre-wrap">
            {post.content}
          </div>

          {/* 達成率 */}
          {post.achievementRate !== null && post.achievementRate !== undefined && (
            <div className="mb-3">
              <div className="flex justify-between mb-1">
                <span style={{ color: '#00ADB5' }} className="text-sm">達成率</span>
                <span style={{ color: '#00ADB5' }} className="text-sm font-bold">
                  {post.achievementRate}%
                </span>
              </div>
              <div className="w-full rounded-full h-2" style={{ backgroundColor: '#222831' }}>
                <div
                  className="h-2 rounded-full transition-all"
                  style={{
                    backgroundColor: '#00ADB5',
                    width: `${post.achievementRate}%`
                  }}
                />
              </div>
            </div>
          )}

          {/* ブロッカー */}
          {post.blockers && (
            <div className="mb-3 p-3 rounded" style={{ backgroundColor: '#222831' }}>
              <span style={{ color: '#FF6B6B' }} className="font-semibold text-sm">🚧 ブロッカー</span>
              <p style={{ color: '#EEEEEE' }} className="text-sm mt-1">{post.blockers}</p>
            </div>
          )}

          {/* 学び */}
          {post.learnings && (
            <div className="mb-3 p-3 rounded" style={{ backgroundColor: '#222831' }}>
              <span style={{ color: '#FFD93D' }} className="font-semibold text-sm">💡 学び</span>
              <p style={{ color: '#EEEEEE' }} className="text-sm mt-1">{post.learnings}</p>
            </div>
          )}

          {/* 次のアクション */}
          {post.nextAction && (
            <div className="mb-3 p-3 rounded" style={{ backgroundColor: '#222831' }}>
              <span style={{ color: '#6BCB77' }} className="font-semibold text-sm">🎯 次のアクション</span>
              <p style={{ color: '#EEEEEE' }} className="text-sm mt-1">{post.nextAction}</p>
            </div>
          )}

          {/* タグ */}
          {post.tags && post.tags.length > 0 && (
            <div className="flex flex-wrap gap-2 mb-4">
              {post.tags.map((tag, index) => (
                <span
                  key={index}
                  className="px-3 py-1 rounded-full text-xs"
                  style={{ backgroundColor: '#00ADB5', color: '#222831' }}
                >
                  #{tag}
                </span>
              ))}
            </div>
          )}

          {/* フッター（リアクション・コメント・閲覧数） */}
          <div className="flex items-center gap-6 pt-4 border-t" style={{ borderColor: '#222831' }}>
            <button
              onClick={() => post.id && handleReaction(post.id)}
              className="flex items-center gap-2 transition-opacity hover:opacity-80"
            >
              <span>👍</span>
              <span style={{ color: '#00ADB5' }} className="text-sm">
                {post.reactionCount || 0}
              </span>
            </button>
            <div className="flex items-center gap-2">
              <span>💬</span>
              <span style={{ color: '#EEEEEE' }} className="text-sm opacity-60">
                {post.commentCount || 0}
              </span>
            </div>
            <div className="flex items-center gap-2">
              <span>👁️</span>
              <span style={{ color: '#EEEEEE' }} className="text-sm opacity-60">
                {post.viewCount || 0}
              </span>
            </div>
          </div>
        </div>
      ))}

      {/* もっと読み込むボタン */}
      {hasMore && (
        <div className="text-center pt-4">
          <button
            onClick={() => setPage(prev => prev + 1)}
            disabled={loading}
            className="px-6 py-3 rounded-lg font-semibold transition-opacity"
            style={{ backgroundColor: '#00ADB5', color: '#EEEEEE' }}
            onMouseEnter={(e) => e.currentTarget.style.opacity = '0.8'}
            onMouseLeave={(e) => e.currentTarget.style.opacity = '1'}
          >
            {loading ? '読み込み中...' : 'もっと見る'}
          </button>
        </div>
      )}
    </div>
  );
}
