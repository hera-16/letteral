'use client';

import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import { formatDistanceToNow } from 'date-fns';
import { ja } from 'date-fns/locale';

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

interface ProgressPostCardProps {
  post: ProgressPost;
  onUpdate?: () => void;
}

export default function ProgressPostCard({ post, onUpdate }: ProgressPostCardProps) {
  const router = useRouter();
  const [showDetails, setShowDetails] = useState(false);

  const handleCardClick = () => {
    router.push(`/progress/${post.id}`);
  };

  const getPostTypeLabel = (type: string) => {
    const labels: { [key: string]: string } = {
      PROGRESS: '進捗報告',
      GOAL: '目標設定',
      CHALLENGE: '課題',
      ACHIEVEMENT: '達成',
      LEARNING: '学び'
    };
    return labels[type] || type;
  };

  const getPostTypeColor = (type: string) => {
    const colors: { [key: string]: string } = {
      PROGRESS: 'bg-blue-100 text-blue-800',
      GOAL: 'bg-green-100 text-green-800',
      CHALLENGE: 'bg-yellow-100 text-yellow-800',
      ACHIEVEMENT: 'bg-purple-100 text-purple-800',
      LEARNING: 'bg-pink-100 text-pink-800'
    };
    return colors[type] || 'bg-gray-100 text-gray-800';
  };

  const getVisibilityIcon = (visibility: string) => {
    switch (visibility) {
      case 'PUBLIC':
        return '🌐';
      case 'ORGANIZATION':
        return '🏢';
      case 'TEAM':
        return '👥';
      case 'PRIVATE':
        return '🔒';
      default:
        return '';
    }
  };

  return (
    <div
      className="bg-white rounded-lg shadow hover:shadow-lg transition-shadow cursor-pointer"
      onClick={handleCardClick}
    >
      <div className="p-6">
        {/* ヘッダー */}
        <div className="flex items-start justify-between mb-4">
          <div className="flex items-center space-x-3">
            {/* プロフィール画像 */}
            <div className="w-10 h-10 rounded-full bg-gradient-to-br from-blue-500 to-purple-500 flex items-center justify-center text-white font-semibold">
              {post.author.displayName.charAt(0)}
            </div>

            <div>
              <div className="flex items-center space-x-2">
                <p className="font-semibold text-gray-900">
                  {post.author.displayName}
                  {post.author.isAnonymous && (
                    <span className="ml-2 text-xs text-gray-500">(匿名)</span>
                  )}
                </p>
                <span className={`px-2 py-1 text-xs font-semibold rounded-full ${getPostTypeColor(post.postType)}`}>
                  {getPostTypeLabel(post.postType)}
                </span>
              </div>
              <div className="flex items-center space-x-2 text-sm text-gray-500 mt-1">
                <span>{post.organization.name}</span>
                <span>•</span>
                <span>{getVisibilityIcon(post.visibility)}</span>
                <span>•</span>
                <span>{formatDistanceToNow(new Date(post.createdAt), { addSuffix: true, locale: ja })}</span>
              </div>
            </div>
          </div>

          {/* Boxタイプバッジ */}
          {post.boxType && (
            <span className="px-3 py-1 text-xs font-medium bg-gray-100 text-gray-700 rounded-full">
              {post.boxType.displayName}
            </span>
          )}
        </div>

        {/* タイトル */}
        {post.title && (
          <h3 className="text-lg font-semibold text-gray-900 mb-2">{post.title}</h3>
        )}

        {/* コンテンツ */}
        <p className="text-gray-700 whitespace-pre-wrap mb-4">{post.content}</p>

        {/* 詳細情報（トグル可能） */}
        {(post.achievementRate !== null && post.achievementRate !== undefined) && (
          <div className="mb-3">
            <div className="flex items-center justify-between text-sm mb-1">
              <span className="text-gray-600">達成率</span>
              <span className="font-semibold text-blue-600">{post.achievementRate}%</span>
            </div>
            <div className="w-full bg-gray-200 rounded-full h-2">
              <div
                className="bg-blue-600 h-2 rounded-full transition-all duration-300"
                style={{ width: `${post.achievementRate}%` }}
              ></div>
            </div>
          </div>
        )}

        {/* 追加情報（折りたたみ可能） */}
        {(post.blockers || post.learnings || post.nextAction) && (
          <div className="mt-3">
            <button
              onClick={(e) => {
                e.stopPropagation();
                setShowDetails(!showDetails);
              }}
              className="text-sm text-blue-600 hover:text-blue-700 font-medium"
            >
              {showDetails ? '詳細を隠す' : '詳細を表示'}
            </button>

            {showDetails && (
              <div className="mt-3 space-y-3 pl-4 border-l-2 border-gray-200">
                {post.blockers && (
                  <div>
                    <h4 className="text-sm font-semibold text-gray-700 mb-1">🚧 ブロッカー</h4>
                    <p className="text-sm text-gray-600">{post.blockers}</p>
                  </div>
                )}

                {post.learnings && (
                  <div>
                    <h4 className="text-sm font-semibold text-gray-700 mb-1">💡 学び</h4>
                    <p className="text-sm text-gray-600">{post.learnings}</p>
                  </div>
                )}

                {post.nextAction && (
                  <div>
                    <h4 className="text-sm font-semibold text-gray-700 mb-1">🎯 次のアクション</h4>
                    <p className="text-sm text-gray-600">{post.nextAction}</p>
                  </div>
                )}
              </div>
            )}
          </div>
        )}

        {/* タグ */}
        {post.tags && post.tags.length > 0 && (
          <div className="flex flex-wrap gap-2 mt-4">
            {post.tags.map((tag, index) => (
              <span
                key={index}
                className="px-2 py-1 text-xs bg-gray-100 text-gray-700 rounded-full hover:bg-gray-200 transition-colors"
              >
                #{tag}
              </span>
            ))}
          </div>
        )}

        {/* フッター（リアクション・コメント） */}
        <div className="flex items-center space-x-6 mt-4 pt-4 border-t border-gray-200 text-sm text-gray-600">
          <button className="flex items-center space-x-1 hover:text-blue-600 transition-colors">
            <span>👍</span>
            <span>{post.reactionCount}</span>
          </button>

          <button className="flex items-center space-x-1 hover:text-blue-600 transition-colors">
            <span>💬</span>
            <span>{post.commentCount}</span>
          </button>

          <div className="flex items-center space-x-1">
            <span>👁️</span>
            <span>{post.viewCount}</span>
          </div>
        </div>
      </div>
    </div>
  );
}
