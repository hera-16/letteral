'use client';

import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import ProgressPostCard from '@/components/progress/ProgressPostCard';
import ProgressPostFilters from '@/components/progress/ProgressPostFilters';
import { progressPostService } from '@/services/progressPostService';

interface ProgressPost {
  id: number;
  postType: string;
  title?: string;
  content: string;
  achievementRate?: number;
  blockers?: string;
  learnings?: string;
  nextAction?: string;
  visibility: string;
  postDate: string;
  tags?: string[];
  author: {
    id: number;
    displayName: string;
    isAnonymous: boolean;
  };
  organization: {
    id: number;
    name: string;
  };
  boxType?: {
    id: number;
    name: string;
    displayName: string;
  };
  reactionCount: number;
  commentCount: number;
  viewCount: number;
  createdAt: string;
}

interface PageInfo {
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export default function ProgressPage() {
  const router = useRouter();
  const [posts, setPosts] = useState<ProgressPost[]>([]);
  const [pageInfo, setPageInfo] = useState<PageInfo>({
    number: 0,
    size: 20,
    totalElements: 0,
    totalPages: 0
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // フィルター状態
  const [filters, setFilters] = useState({
    tenantId: 1, // TODO: 実際のテナントIDを取得
    organizationId: null as number | null,
    postType: null as string | null,
    startDate: null as string | null,
    endDate: null as string | null,
    page: 0,
    size: 20
  });

  useEffect(() => {
    fetchPosts();
  }, [filters]);

  const fetchPosts = async () => {
    try {
      setLoading(true);
      setError(null);

      const response = await progressPostService.getTimeline(filters);

      setPosts(response.content);
      setPageInfo(response.page);
    } catch (err: any) {
      console.error('進捗投稿の取得に失敗しました', err);
      setError('進捗投稿の取得に失敗しました');
    } finally {
      setLoading(false);
    }
  };

  const handlePageChange = (newPage: number) => {
    setFilters(prev => ({ ...prev, page: newPage }));
  };

  const handleFilterChange = (newFilters: Partial<typeof filters>) => {
    setFilters(prev => ({ ...prev, ...newFilters, page: 0 }));
  };

  const handleCreatePost = () => {
    router.push('/progress/new');
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* ヘッダー */}
      <div className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <div className="flex justify-between items-center">
            <div>
              <h1 className="text-3xl font-bold text-gray-900">進捗タイムライン</h1>
              <p className="mt-1 text-sm text-gray-500">
                チームの日々の進捗・目標・課題を共有
              </p>
            </div>
            <button
              onClick={handleCreatePost}
              className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
            >
              新規投稿
            </button>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
          {/* フィルターサイドバー */}
          <div className="lg:col-span-1">
            <ProgressPostFilters
              filters={filters}
              onFilterChange={handleFilterChange}
            />
          </div>

          {/* メインコンテンツ */}
          <div className="lg:col-span-3">
            {/* ローディング */}
            {loading && (
              <div className="text-center py-12">
                <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
                <p className="mt-4 text-gray-600">読み込み中...</p>
              </div>
            )}

            {/* エラー */}
            {error && !loading && (
              <div className="bg-red-50 border border-red-200 rounded-lg p-4 text-red-800">
                {error}
              </div>
            )}

            {/* 投稿リスト */}
            {!loading && !error && (
              <>
                {posts.length === 0 ? (
                  <div className="bg-white rounded-lg shadow p-12 text-center">
                    <p className="text-gray-500 text-lg">投稿がありません</p>
                    <button
                      onClick={handleCreatePost}
                      className="mt-4 px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
                    >
                      最初の投稿を作成
                    </button>
                  </div>
                ) : (
                  <div className="space-y-4">
                    {posts.map(post => (
                      <ProgressPostCard
                        key={post.id}
                        post={post}
                        onUpdate={fetchPosts}
                      />
                    ))}
                  </div>
                )}

                {/* ページネーション */}
                {pageInfo.totalPages > 1 && (
                  <div className="mt-8 flex justify-center">
                    <nav className="inline-flex rounded-md shadow-sm -space-x-px">
                      <button
                        onClick={() => handlePageChange(pageInfo.number - 1)}
                        disabled={pageInfo.number === 0}
                        className="relative inline-flex items-center px-4 py-2 rounded-l-md border border-gray-300 bg-white text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:bg-gray-100 disabled:cursor-not-allowed"
                      >
                        前へ
                      </button>

                      {/* ページ番号 */}
                      {Array.from({ length: pageInfo.totalPages }, (_, i) => (
                        <button
                          key={i}
                          onClick={() => handlePageChange(i)}
                          className={`relative inline-flex items-center px-4 py-2 border text-sm font-medium ${
                            i === pageInfo.number
                              ? 'z-10 bg-blue-600 border-blue-600 text-white'
                              : 'bg-white border-gray-300 text-gray-700 hover:bg-gray-50'
                          }`}
                        >
                          {i + 1}
                        </button>
                      ))}

                      <button
                        onClick={() => handlePageChange(pageInfo.number + 1)}
                        disabled={pageInfo.number >= pageInfo.totalPages - 1}
                        className="relative inline-flex items-center px-4 py-2 rounded-r-md border border-gray-300 bg-white text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:bg-gray-100 disabled:cursor-not-allowed"
                      >
                        次へ
                      </button>
                    </nav>
                  </div>
                )}

                {/* 投稿数表示 */}
                <div className="mt-4 text-center text-sm text-gray-600">
                  全 {pageInfo.totalElements} 件中 {pageInfo.number * pageInfo.size + 1} - {Math.min((pageInfo.number + 1) * pageInfo.size, pageInfo.totalElements)} 件を表示
                </div>
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
