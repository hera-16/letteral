'use client';

import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import ProgressPostForm from '@/components/progress/ProgressPostForm';
import { progressPostService } from '@/services/progressPostService';

export default function NewProgressPostPage() {
  const router = useRouter();
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (data: any) => {
    try {
      setSubmitting(true);
      setError(null);

      await progressPostService.createPost(data);

      // 成功したらタイムラインに戻る
      router.push('/progress');
    } catch (err: any) {
      console.error('進捗投稿の作成に失敗しました', err);
      setError('進捗投稿の作成に失敗しました: ' + (err.message || ''));
    } finally {
      setSubmitting(false);
    }
  };

  const handleCancel = () => {
    router.back();
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* ヘッダー */}
      <div className="bg-white shadow">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <h1 className="text-3xl font-bold text-gray-900">新規進捗投稿</h1>
          <p className="mt-1 text-sm text-gray-500">
            今日の目標・達成・課題・学びを記録しましょう
          </p>
        </div>
      </div>

      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* エラー表示 */}
        {error && (
          <div className="mb-6 bg-red-50 border border-red-200 rounded-lg p-4 text-red-800">
            {error}
          </div>
        )}

        {/* フォーム */}
        <ProgressPostForm
          onSubmit={handleSubmit}
          onCancel={handleCancel}
          submitting={submitting}
        />
      </div>
    </div>
  );
}
