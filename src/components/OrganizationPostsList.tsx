'use client';

import { useState, useEffect } from 'react';
import { organizationService, progressPostService, Organization, ProgressPost, PagedProgressPosts } from '@/services/api';
import ProgressPostCard from '@/components/progress/ProgressPostCard';

interface OrganizationPostsListProps {
  userId: number;
  tenantId: number;
  authorId: number;
  primaryOrganizationId: number;
}

export default function OrganizationPostsList({
  userId,
  tenantId,
  authorId,
  primaryOrganizationId
}: OrganizationPostsListProps) {
  const [organizations, setOrganizations] = useState<Organization[]>([]);
  const [selectedOrganizationId, setSelectedOrganizationId] = useState<number | null>(null);
  const [posts, setPosts] = useState<ProgressPost[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadingPosts, setLoadingPosts] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(false);

  useEffect(() => {
    loadUserOrganizations();
  }, [userId]);

  useEffect(() => {
    if (selectedOrganizationId) {
      setPage(0);
      setPosts([]);
      loadOrganizationPosts(0);
    }
  }, [selectedOrganizationId]);

  useEffect(() => {
    if (selectedOrganizationId && page > 0) {
      loadOrganizationPosts(page);
    }
  }, [page]);

  const loadUserOrganizations = async () => {
    try {
      setLoading(true);
      setError(null);
      const orgs = await organizationService.getUserAccessibleOrganizations(userId);
      setOrganizations(orgs);
    } catch (err: any) {
      console.error('組織一覧の読み込みエラー:', err);
      setError(err.response?.data?.message || '組織一覧の読み込みに失敗しました');
    } finally {
      setLoading(false);
    }
  };

  const loadOrganizationPosts = async (currentPage: number) => {
    if (!selectedOrganizationId) return;

    try {
      setLoadingPosts(true);
      setError(null);

      const response: PagedProgressPosts = await progressPostService.getOrganizationHierarchicalPosts(
        selectedOrganizationId,
        userId,
        currentPage,
        10
      );

      if (currentPage === 0) {
        setPosts(response.content);
      } else {
        setPosts(prev => [...prev, ...response.content]);
      }

      setHasMore(!response.last);
    } catch (err: any) {
      console.error('投稿の読み込みエラー:', err);
      setError(err.response?.data?.message || '投稿の読み込みに失敗しました');
    } finally {
      setLoadingPosts(false);
    }
  };

  const handleOrganizationClick = (organizationId: number) => {
    setSelectedOrganizationId(organizationId);
  };

  if (loading) {
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

  return (
    <div className="space-y-6">
      {/* グループ一覧 */}
      <div className="rounded-lg p-6" style={{ backgroundColor: '#393E46' }}>
        <h2 className="text-xl font-bold mb-4" style={{ color: '#EEEEEE' }}>
          🏢 所属グループ
        </h2>
        {organizations.length === 0 ? (
          <p style={{ color: '#EEEEEE' }} className="text-sm">所属しているグループがありません</p>
        ) : (
          <div className="space-y-2">
            {organizations.map(org => (
              <button
                key={org.id}
                onClick={() => handleOrganizationClick(org.id)}
                className={`w-full text-left px-4 py-3 rounded-lg transition-all hover:shadow-lg ${
                  selectedOrganizationId === org.id
                    ? 'font-bold'
                    : ''
                }`}
                style={{
                  backgroundColor: selectedOrganizationId === org.id ? '#00ADB5' : '#222831',
                  color: '#EEEEEE'
                }}
                onMouseEnter={(e) => {
                  if (selectedOrganizationId !== org.id) {
                    e.currentTarget.style.backgroundColor = '#2c3238';
                    e.currentTarget.style.transform = 'translateX(4px)';
                  }
                }}
                onMouseLeave={(e) => {
                  if (selectedOrganizationId !== org.id) {
                    e.currentTarget.style.backgroundColor = '#222831';
                    e.currentTarget.style.transform = 'translateX(0)';
                  }
                }}
              >
                <div className="flex items-center justify-between">
                  <span>{org.name}</span>
                  {selectedOrganizationId === org.id && <span>▶</span>}
                </div>
              </button>
            ))}
          </div>
        )}
      </div>

      {/* 選択されたグループの投稿 */}
      {selectedOrganizationId && (
        <div className="rounded-lg p-6" style={{ backgroundColor: '#393E46' }}>
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-xl font-bold" style={{ color: '#EEEEEE' }}>
              📋 投稿一覧
            </h2>
            <button
              onClick={() => {
                setSelectedOrganizationId(null);
                setPosts([]);
              }}
              className="text-sm px-3 py-1 rounded transition-opacity"
              style={{ backgroundColor: '#00ADB5', color: '#EEEEEE' }}
              onMouseEnter={(e) => e.currentTarget.style.opacity = '0.8'}
              onMouseLeave={(e) => e.currentTarget.style.opacity = '1'}
            >
              ✕ 閉じる
            </button>
          </div>

          {loadingPosts && page === 0 ? (
            <div className="flex items-center justify-center py-8">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2" style={{ borderColor: '#00ADB5' }}></div>
              <span className="ml-3" style={{ color: '#EEEEEE' }}>読み込み中...</span>
            </div>
          ) : posts.length === 0 ? (
            <div className="text-center py-8">
              <p style={{ color: '#EEEEEE' }} className="text-lg mb-2">📝 まだ投稿がありません</p>
              <p style={{ color: '#00ADB5' }} className="text-sm">最初の進捗を投稿してみましょう!</p>
            </div>
          ) : (
            <div className="space-y-4">
              {posts.map((post) => (
                <ProgressPostCard
                  key={post.id}
                  post={{
                    id: post.id!,
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
                      id: post.authorId,
                      displayName: post.formattedAnonymousNumber || `#${String(post.anonymousNumber).padStart(4, '0')}`,
                      isAnonymous: post.isAnonymous || true,
                    },
                    organization: {
                      id: post.organizationId,
                      name: post.organizationName || '',
                    },
                    boxType: post.boxType,
                    createdAt: post.createdAt || post.postDate,
                  }}
                  onUpdate={() => loadOrganizationPosts(0)}
                  currentUserId={userId}
                />
              ))}

              {/* もっと読み込むボタン */}
              {hasMore && (
                <div className="text-center pt-4">
                  <button
                    onClick={() => setPage(prev => prev + 1)}
                    disabled={loadingPosts}
                    className="px-6 py-3 rounded-lg font-semibold transition-opacity"
                    style={{ backgroundColor: '#00ADB5', color: '#EEEEEE' }}
                    onMouseEnter={(e) => e.currentTarget.style.opacity = '0.8'}
                    onMouseLeave={(e) => e.currentTarget.style.opacity = '1'}
                  >
                    {loadingPosts ? '読み込み中...' : 'もっと見る'}
                  </button>
                </div>
              )}
            </div>
          )}
        </div>
      )}
    </div>
  );
}
