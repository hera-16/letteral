'use client';

import { useEffect, useState } from 'react';
import { UserBadge } from '@/services/api';

interface BadgeNotificationModalProps {
  badges: UserBadge[];
  onClose: () => void;
}

export default function BadgeNotificationModal({ badges, onClose }: BadgeNotificationModalProps) {
  const [currentIndex, setCurrentIndex] = useState(0);
  const [show, setShow] = useState(true);

  useEffect(() => {
    // 3秒後に自動的に次のバッジまたは閉じる
    const timer = setTimeout(() => {
      if (currentIndex < badges.length - 1) {
        setCurrentIndex(currentIndex + 1);
      } else {
        handleClose();
      }
    }, 3000);

    return () => clearTimeout(timer);
  }, [currentIndex, badges.length]);

  const handleClose = () => {
    setShow(false);
    setTimeout(onClose, 300); // アニメーション後に閉じる
  };

  const currentBadge = badges[currentIndex];

  if (!currentBadge || badges.length === 0) {
    return null;
  }

  return (
    <div
      className={`fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm transition-opacity duration-300 ${
        show ? 'opacity-100' : 'opacity-0'
      }`}
      onClick={handleClose}
    >
      <div
        className={`bg-white rounded-2xl p-8 max-w-md w-full mx-4 shadow-2xl transform transition-all duration-300 ${
          show ? 'scale-100' : 'scale-95'
        }`}
        onClick={(e) => e.stopPropagation()}
      >
        {/* 紙吹雪エフェクト */}
        <div className="absolute inset-0 pointer-events-none overflow-hidden rounded-2xl">
          {[...Array(30)].map((_, i) => (
            <div
              key={i}
              className="absolute animate-confetti text-2xl"
              style={{
                left: `${Math.random() * 100}%`,
                top: '-20px',
                animationDelay: `${Math.random() * 2}s`,
                animationDuration: `${2 + Math.random() * 2}s`,
              }}
            >
              {['🎉', '✨', '⭐', '🌟', '💫', '🎊'][Math.floor(Math.random() * 6)]}
            </div>
          ))}
        </div>

        {/* コンテンツ */}
        <div className="relative text-center">
          {/* ヘッダー */}
          <div className="mb-6">
            <h2 className="text-3xl font-bold bg-gradient-to-r from-purple-600 to-pink-600 bg-clip-text text-transparent mb-2">
              🎉 新しいバッジ獲得！
            </h2>
            <p className="text-gray-600">
              おめでとうございます！素晴らしい成果です！
            </p>
          </div>

          {/* バッジアイコン */}
          <div className="mb-6 animate-bounce-in">
            <div className="text-8xl mb-4 animate-spin-slow">
              {currentBadge.badge.icon}
            </div>
            <h3 className="text-2xl font-bold text-gray-800 mb-2">
              {currentBadge.badge.name}
            </h3>
            <p className="text-gray-600">
              {currentBadge.badge.description}
            </p>
          </div>

          {/* 進捗インジケーター */}
          {badges.length > 1 && (
            <div className="flex justify-center gap-2 mb-4">
              {badges.map((_, index) => (
                <div
                  key={index}
                  className={`h-2 rounded-full transition-all duration-300 ${
                    index === currentIndex
                      ? 'w-8 bg-purple-600'
                      : index < currentIndex
                      ? 'w-2 bg-purple-400'
                      : 'w-2 bg-gray-300'
                  }`}
                />
              ))}
            </div>
          )}

          {/* ボタン */}
          <div className="flex gap-3 justify-center">
            {currentIndex < badges.length - 1 ? (
              <>
                <button
                  onClick={() => setCurrentIndex(currentIndex + 1)}
                  className="px-6 py-3 bg-gradient-to-r from-purple-600 to-pink-600 text-white rounded-lg font-semibold hover:from-purple-700 hover:to-pink-700 transition-all duration-300 shadow-lg hover:shadow-xl"
                >
                  次へ →
                </button>
                <button
                  onClick={handleClose}
                  className="px-6 py-3 bg-gray-200 text-gray-700 rounded-lg font-semibold hover:bg-gray-300 transition-colors"
                >
                  スキップ
                </button>
              </>
            ) : (
              <button
                onClick={handleClose}
                className="px-8 py-3 bg-gradient-to-r from-purple-600 to-pink-600 text-white rounded-lg font-semibold hover:from-purple-700 hover:to-pink-700 transition-all duration-300 shadow-lg hover:shadow-xl"
              >
                閉じる
              </button>
            )}
          </div>

          {/* バッジ一覧へのリンク */}
          <div className="mt-4">
            <a
              href="/badges"
              className="text-purple-600 hover:text-purple-700 text-sm font-medium underline"
              onClick={handleClose}
            >
              すべてのバッジを見る
            </a>
          </div>
        </div>
      </div>
    </div>
  );
}
