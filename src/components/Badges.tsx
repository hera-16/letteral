'use client';

import { useState, useEffect } from 'react';
import { badgeService, UserBadge } from '@/services/api';

export default function Badges() {
  const [badges, setBadges] = useState<UserBadge[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadBadges();
  }, []);

  const loadBadges = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await badgeService.getUserBadges();
      setBadges(data);
    } catch (error: any) {
      console.error('バッジの読み込みに失敗しました:', error);
      setError('バッジの読み込みに失敗しました');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-purple-600 mx-auto mb-4"></div>
          <p className="text-gray-600">バッジを読み込み中...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-50 border border-red-200 rounded-lg p-6 text-center">
        <p className="text-red-600 text-lg font-medium">⚠️ {error}</p>
        <button
          onClick={loadBadges}
          className="mt-4 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors"
        >
          再試行
        </button>
      </div>
    );
  }

  const getBadgeStyle = (isNew: boolean) => {
    return isNew
      ? 'bg-gradient-to-br from-yellow-100 to-amber-100 border-yellow-400 shadow-lg animate-pulse-glow'
      : 'bg-white border-gray-200';
  };

  return (
    <div className="space-y-6">
      {/* ヘッダー */}
      <div className="bg-gradient-to-r from-purple-600 to-pink-600 rounded-xl p-6 text-white">
        <h2 className="text-3xl font-bold mb-2">🏆 獲得バッジ</h2>
        <p className="text-purple-100">
          あなたが達成した素晴らしい成果の記録です！
        </p>
        <div className="mt-4 flex items-center gap-4">
          <div className="bg-white/20 backdrop-blur-sm rounded-lg px-4 py-2">
            <span className="text-2xl font-bold">{badges.length}</span>
            <span className="text-sm ml-2">個獲得</span>
          </div>
          {badges.filter(b => b.isNew).length > 0 && (
            <div className="bg-yellow-400 text-yellow-900 rounded-lg px-4 py-2 font-semibold animate-bounce-in">
              🎉 新バッジ {badges.filter(b => b.isNew).length}個！
            </div>
          )}
        </div>
      </div>

      {/* バッジがない場合 */}
      {badges.length === 0 ? (
        <div className="bg-gradient-to-br from-gray-50 to-gray-100 rounded-xl p-12 text-center border-2 border-dashed border-gray-300">
          <div className="text-6xl mb-4">🌱</div>
          <h3 className="text-2xl font-bold text-gray-700 mb-2">
            まだバッジがありません
          </h3>
          <p className="text-gray-600 mb-6">
            チャレンジを達成して、最初のバッジを獲得しましょう！
          </p>
          <a
            href="/"
            className="inline-block px-6 py-3 bg-purple-600 text-white rounded-lg font-semibold hover:bg-purple-700 transition-colors"
          >
            チャレンジを始める
          </a>
        </div>
      ) : (
        /* バッジグリッド */
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {badges.map((userBadge) => (
            <div
              key={userBadge.id}
              className={`border-2 rounded-xl p-6 transition-all duration-300 hover:scale-105 hover:shadow-xl ${getBadgeStyle(userBadge.isNew)}`}
            >
              {/* 新バッジリボン */}
              {userBadge.isNew && (
                <div className="absolute -top-2 -right-2 bg-gradient-to-r from-yellow-400 to-orange-400 text-white text-xs font-bold px-3 py-1 rounded-full shadow-lg animate-bounce-in">
                  NEW!
                </div>
              )}

              {/* バッジアイコン */}
              <div className="text-center mb-4">
                <div className="text-6xl mb-3 animate-bounce-in">
                  {userBadge.badge.icon}
                </div>
                <h3 className="text-xl font-bold text-gray-800 mb-1">
                  {userBadge.badge.name}
                </h3>
                <p className="text-sm text-gray-600">
                  {userBadge.badge.description}
                </p>
              </div>

              {/* 獲得日時 */}
              <div className="border-t pt-3 mt-3">
                <p className="text-xs text-gray-500 text-center">
                  獲得日時
                </p>
                <p className="text-sm text-gray-700 text-center font-medium">
                  {new Date(userBadge.earnedAt).toLocaleDateString('ja-JP', {
                    year: 'numeric',
                    month: 'long',
                    day: 'numeric',
                  })}
                </p>
              </div>

              {/* バッジタイプ */}
              <div className="mt-2 text-center">
                <span className="inline-block bg-gray-100 text-gray-700 text-xs px-3 py-1 rounded-full">
                  {getBadgeTypeLabel(userBadge.badge.badgeType)}
                </span>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* バッジ統計 */}
      {badges.length > 0 && (
        <div className="bg-gradient-to-br from-indigo-50 to-purple-50 rounded-xl p-6 border border-indigo-200">
          <h3 className="text-xl font-bold text-gray-800 mb-4">
            📊 バッジ統計
          </h3>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <StatCard
              icon="🏆"
              label="総獲得数"
              value={badges.length}
            />
            <StatCard
              icon="🌟"
              label="新バッジ"
              value={badges.filter(b => b.isNew).length}
            />
            <StatCard
              icon="🔥"
              label="連続系"
              value={badges.filter(b => b.badge.badgeType.includes('STREAK')).length}
            />
            <StatCard
              icon="🎯"
              label="達成系"
              value={badges.filter(b => b.badge.badgeType.includes('TOTAL')).length}
            />
          </div>
        </div>
      )}
    </div>
  );
}

// バッジタイプのラベルを取得
function getBadgeTypeLabel(badgeType: string): string {
  const labels: Record<string, string> = {
    'FIRST_STEP': '初回達成',
    'STREAK_3': '3日連続',
    'STREAK_7': '7日連続',
    'TOTAL_10': '10回達成',
    'TOTAL_30': '30回達成',
    'TOTAL_50': '50回達成',
    'GRATITUDE_10': '感謝マスター',
    'KINDNESS_10': '優しさの達人',
    'SELF_CARE_10': 'セルフケア上手',
    'CREATIVITY_10': 'クリエイター',
    'CONNECTION_10': 'コミュニケーター',
    'LEVEL_3': '花レベル3',
    'LEVEL_5': '花レベル5',
    'LEVEL_7': '花レベル7',
    'LEVEL_10': '花レベル10',
  };
  return labels[badgeType] || badgeType;
}

// 統計カードコンポーネント
function StatCard({ icon, label, value }: { icon: string; label: string; value: number }) {
  return (
    <div className="bg-white rounded-lg p-4 text-center shadow-sm border border-gray-200">
      <div className="text-3xl mb-2">{icon}</div>
      <div className="text-2xl font-bold text-gray-800">{value}</div>
      <div className="text-xs text-gray-600">{label}</div>
    </div>
  );
}
