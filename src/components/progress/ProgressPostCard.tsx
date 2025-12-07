'use client';

import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { formatDistanceToNow } from 'date-fns';
import { ja } from 'date-fns/locale';
import { postReplyService, PostReply } from '@/services/api';

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
  createdAt: string;
}

interface ProgressPostCardProps {
  post: ProgressPost;
  onUpdate?: () => void;
  currentUserId?: number;
}

export default function ProgressPostCard({ post, onUpdate, currentUserId }: ProgressPostCardProps) {
  const router = useRouter();
  const [showDetails, setShowDetails] = useState(false);
  const [showReplyForm, setShowReplyForm] = useState(false);
  const [showReplies, setShowReplies] = useState(false);
  const [replyContent, setReplyContent] = useState('');
  const [replies, setReplies] = useState<PostReply[]>([]);
  const [replyCount, setReplyCount] = useState(0);
  const [isSubmittingReply, setIsSubmittingReply] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  // 返信数を取得
  useEffect(() => {
    loadReplyCount();
  }, [post.id]);

  const loadReplyCount = async () => {
    try {
      const count = await postReplyService.getReplyCount(post.id);
      setReplyCount(count);
    } catch (error) {
      console.error('Failed to load reply count:', error);
    }
  };

  // 返信一覧を取得
  const loadReplies = async () => {
    try {
      const fetchedReplies = await postReplyService.getRepliesByPostId(post.id);
      setReplies(fetchedReplies);
      setShowReplies(true);
    } catch (error) {
      console.error('Failed to load replies:', error);
      setErrorMessage('返信の読み込みに失敗しました');
    }
  };

  // 返信を送信
  const handleSubmitReply = async (e: React.FormEvent) => {
    e.preventDefault();
    e.stopPropagation();

    if (!replyContent.trim()) {
      setErrorMessage('返信内容を入力してください');
      return;
    }

    setIsSubmittingReply(true);
    setErrorMessage('');

    try {
      await postReplyService.createReply({
        postId: post.id,
        content: replyContent,
      });

      setReplyContent('');
      setShowReplyForm(false);

      // 返信数を更新
      await loadReplyCount();

      // 返信一覧が表示されている場合は再読み込み
      if (showReplies) {
        await loadReplies();
      }
    } catch (error: any) {
      console.error('Failed to submit reply:', error);
      console.error('Error response data:', error.response?.data);
      const message = error.response?.data?.message || error.message || '返信の送信に失敗しました';
      setErrorMessage(message);
    } finally {
      setIsSubmittingReply(false);
    }
  };

  const handleCardClick = () => {
    router.push(`/progress/${post.id}`);
  };

  const getPostTypeLabel = (type: string) => {
    const labels: { [key: string]: string } = {
      PROGRESS: '進捗報告',
      GOAL: '目標設定',
      CHALLENGE: '課題',
      ACHIEVEMENT: '達成',
      LEARNING: '学び',
      QUESTION: '質問'
    };
    return labels[type] || type;
  };

  const getPostTypeColor = (type: string) => {
    const colors: { [key: string]: string } = {
      PROGRESS: 'bg-blue-100 text-blue-800',
      GOAL: 'bg-green-100 text-green-800',
      CHALLENGE: 'bg-yellow-100 text-yellow-800',
      ACHIEVEMENT: 'bg-purple-100 text-purple-800',
      LEARNING: 'bg-pink-100 text-pink-800',
      QUESTION: 'bg-orange-100 text-orange-800'
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

  // 返信権限のチェック: ログインしているユーザー全員に表示（バックエンドで権限チェック）
  const canUserReply = !!currentUserId;

  return (
    <div className="bg-white rounded-lg shadow hover:shadow-lg transition-shadow">
      <div
        className="p-6 cursor-pointer"
        onClick={handleCardClick}
      >
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
      </div>

      {/* 返信セクション */}
      <div
        className="px-6 pb-4 border-t border-gray-100"
        onClick={(e) => e.stopPropagation()}
      >
        {/* 返信数表示と返信一覧トグル */}
        {replyCount > 0 && (
          <button
            onClick={async () => {
              if (!showReplies) {
                await loadReplies();
              } else {
                setShowReplies(false);
              }
            }}
            className="text-sm text-gray-600 hover:text-gray-800 mt-3"
          >
            💬 {replyCount}件の返信 {showReplies ? '▼' : '▶'}
          </button>
        )}

        {/* 返信一覧 */}
        {showReplies && replies.length > 0 && (
          <div className="mt-3 space-y-2 bg-gray-50 rounded-lg p-3">
            {replies.map((reply) => (
              <div key={reply.id} className="bg-white rounded p-3 text-sm">
                <div className="flex items-center space-x-2 mb-1">
                  <span className="font-semibold text-gray-800">{reply.authorName}</span>
                  <span className="text-xs text-gray-500">
                    {formatDistanceToNow(new Date(reply.createdAt), { addSuffix: true, locale: ja })}
                  </span>
                </div>
                <p className="text-gray-700 whitespace-pre-wrap">{reply.content}</p>
              </div>
            ))}
          </div>
        )}

        {/* 返信ボタン */}
        {canUserReply && !showReplyForm && (
          <button
            onClick={(e) => {
              e.stopPropagation();
              setShowReplyForm(true);
            }}
            className="mt-3 text-sm text-blue-600 hover:text-blue-700 font-medium"
          >
            ✉️ 返信する
          </button>
        )}

        {/* 返信入力フォーム */}
        {showReplyForm && (
          <form onSubmit={handleSubmitReply} className="mt-3 space-y-2">
            {errorMessage && (
              <div className="text-sm text-red-600 bg-red-50 p-2 rounded">
                {errorMessage}
              </div>
            )}
            <textarea
              value={replyContent}
              onChange={(e) => setReplyContent(e.target.value)}
              placeholder="返信を入力してください..."
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none"
              rows={3}
              disabled={isSubmittingReply}
            />
            <div className="flex space-x-2">
              <button
                type="submit"
                disabled={isSubmittingReply || !replyContent.trim()}
                className="px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-lg hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed"
              >
                {isSubmittingReply ? '送信中...' : '送信'}
              </button>
              <button
                type="button"
                onClick={(e) => {
                  e.stopPropagation();
                  setShowReplyForm(false);
                  setReplyContent('');
                  setErrorMessage('');
                }}
                disabled={isSubmittingReply}
                className="px-4 py-2 bg-gray-200 text-gray-700 text-sm font-medium rounded-lg hover:bg-gray-300 disabled:cursor-not-allowed"
              >
                キャンセル
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
}
