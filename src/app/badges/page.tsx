'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import Badges from '@/components/Badges';

export default function BadgesPage() {
  const router = useRouter();

  useEffect(() => {
    // 認証チェック
    const token = localStorage.getItem('token');
    if (!token) {
      router.push('/');
    }
  }, [router]);

  return (
    <div className="min-h-screen bg-gradient-to-br from-purple-50 via-pink-50 to-blue-50">
      <div className="container mx-auto px-4 py-8">
        {/* ナビゲーションバー */}
        <div className="mb-6">
          <button
            onClick={() => router.push('/')}
            className="flex items-center gap-2 text-gray-600 hover:text-gray-800 transition-colors"
          >
            <span className="text-xl">←</span>
            <span className="font-medium">ホームに戻る</span>
          </button>
        </div>

        {/* バッジコンポーネント */}
        <Badges />
      </div>
    </div>
  );
}
