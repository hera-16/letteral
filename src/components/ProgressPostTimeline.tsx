'use client';

import { useState, useEffect } from 'react';
import { progressPostService, ProgressPost, PagedProgressPosts } from '@/services/api';
import ProgressPostCard from '@/components/progress/ProgressPostCard';

interface ProgressPostTimelineProps {
  tenantId: number;
  organizationId?: number;
  userId?: number;
  selectedOrganizationId?: number | null;
}

export default function ProgressPostTimeline({
  tenantId,
  organizationId,
  userId,
  selectedOrganizationId
}: ProgressPostTimelineProps) {
  const [posts, setPosts] = useState<ProgressPost[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(false);
  const [totalElements, setTotalElements] = useState(0);

  useEffect(() => {
    setPage(0); // 選択が変わったらページをリセット
    setPosts([]); // 投稿をクリア
  }, [selectedOrganizationId]);

  useEffect(() => {
    loadPosts();
  }, [tenantId, organizationId, userId, selectedOrganizationId, page]);

  const loadPosts = async () => {
    try {
      setLoading(true);
      setError(null);

      let response: PagedProgressPosts;

      // 組織が選択されている場合は、その組織の階層的な投稿を取得
      if (selectedOrganizationId && userId) {
        response = await progressPostService.getOrganizationHierarchicalPosts(
          selectedOrganizationId,
          userId,
          page,
          10
        );
      }
      // ユーザーIDがある場合は、ユーザーが閲覧可能な投稿を取得
      else if (userId && !selectedOrganizationId) {
        response = await progressPostService.getViewablePostsForUser(
          userId,
          tenantId,
          page,
          10
        );
      }
      // 従来の動作: 組織IDまたはテナントIDで取得
      else if (organizationId) {
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
        <div>
          <h2 className="text-2xl font-bold" style={{ color: '#EEEEEE' }}>
            📋 進捗タイムライン
          </h2>
          {selectedOrganizationId && (
            <p className="text-sm mt-1" style={{ color: '#00ADB5' }}>
              選択された組織とその配下の投稿を表示中
            </p>
          )}
        </div>
        <span style={{ color: '#00ADB5' }} className="text-sm">
          {totalElements} 件の投稿
        </span>
      </div>

      {posts.map((post) => (
        <ProgressPostCard
          key={post.id}
          post={{
            id: post.id,
            postType: post.postType,
            title: post.title,
            content: post.content,
            achievementRate: post.achievementRate,
            blockers: post.blockers,
            learnings: post.learnings,
            nextAction: post.nextAction,
            visibility: post.visibility || 'ORGANIZATION',
            postDate: post.postDate,
            tags: post.tags,
            author: {
              id: post.authorId || 0,
              displayName: post.formattedAnonymousNumber || `#${String(post.anonymousNumber).padStart(4, '0')}`,
              isAnonymous: true,
            },
            organization: {
              id: post.organizationId || 0,
              name: post.organizationName || '',
            },
            boxType: post.boxType,
            createdAt: post.createdAt || post.postDate,
          }}
          onUpdate={loadPosts}
          currentUserId={userId}
        />
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
