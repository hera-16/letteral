'use client';

import { useState, useEffect } from 'react';
import { badgeService, UserBadge } from '@/services/api';

interface BadgesProps {
  onNavigateToChallenges?: () => void;
}

export default function Badges({ onNavigateToChallenges }: BadgesProps) {
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
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 mx-auto mb-4" style={{ borderColor: '#00ADB5' }}></div>
          <p style={{ color: '#EEEEEE' }}>バッジを読み込み中...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div style={{ backgroundColor: '#393E46', border: '1px solid #ff6b6b', borderRadius: '0.5rem', padding: '1.5rem', textAlign: 'center' }}>
        <p style={{ color: '#ff6b6b', fontSize: '1.125rem', fontWeight: 500 }}>⚠️ {error}</p>
        <button
          onClick={loadBadges}
          style={{ marginTop: '1rem', padding: '0.5rem 1rem', backgroundColor: '#ff6b6b', color: '#EEEEEE', border: 'none', borderRadius: '0.5rem', cursor: 'pointer' }}
        >
          再試行
        </button>
      </div>
    );
  }

  const getBadgeStyle = (isNew: boolean) => {
    return isNew
      ? { backgroundColor: '#393E46', border: '2px solid #00ADB5', boxShadow: '0 0 20px rgba(0, 173, 181, 0.5)' }
      : { backgroundColor: '#222831', border: '2px solid #393E46' };
  };

  return (
    <div className="space-y-6">
      {/* ヘッダー */}
      <div style={{ background: 'linear-gradient(to right, #00ADB5, #393E46)', borderRadius: '0.75rem', padding: '1.5rem', color: '#EEEEEE' }}>
        <h2 className="text-3xl font-bold mb-2">🏆 獲得バッジ</h2>
        <p style={{ color: '#EEEEEE', opacity: 0.9 }}>
          あなたが達成した素晴らしい成果の記録です!
        </p>
        <div className="mt-4 flex items-center gap-4">
          <div style={{ backgroundColor: 'rgba(34, 40, 49, 0.8)', backdropFilter: 'blur(10px)', borderRadius: '0.5rem', padding: '0.5rem 1rem' }}>
            <span className="text-2xl font-bold">{badges.length}</span>
            <span className="text-sm ml-2">個獲得</span>
          </div>
          {badges.filter(b => b.isNew).length > 0 && (
            <div style={{ backgroundColor: '#00ADB5', color: '#222831', borderRadius: '0.5rem', padding: '0.5rem 1rem', fontWeight: 600 }}>
              🎉 新バッジ {badges.filter(b => b.isNew).length}個!
            </div>
          )}
        </div>
      </div>

      {/* バッジがない場合 */}
      {badges.length === 0 ? (
        <div style={{ background: 'linear-gradient(to bottom right, #393E46, #222831)', borderRadius: '0.75rem', padding: '3rem', textAlign: 'center', border: '2px dashed #00ADB5' }}>
          <div className="text-6xl mb-4">🌱</div>
          <h3 style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#EEEEEE', marginBottom: '0.5rem' }}>
            まだバッジがありません
          </h3>
          <p style={{ color: '#EEEEEE', opacity: 0.8, marginBottom: '1.5rem' }}>
            チャレンジを達成して、最初のバッジを獲得しましょう!
          </p>
          <button
            onClick={() => onNavigateToChallenges?.()}
            style={{ display: 'inline-block', padding: '0.75rem 1.5rem', backgroundColor: '#00ADB5', color: '#EEEEEE', borderRadius: '0.5rem', fontWeight: 600, border: 'none', cursor: 'pointer' }}
          >
            チャレンジを始める
          </button>
        </div>
      ) : (
        /* バッジグリッド */
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {badges.map((userBadge) => (
            <div
              key={userBadge.id}
              style={{
                ...getBadgeStyle(userBadge.isNew),
                borderRadius: '0.75rem',
                padding: '1.5rem',
                transition: 'all 0.3s',
                position: 'relative'
              }}
              onMouseEnter={(e) => {
                e.currentTarget.style.transform = 'scale(1.05)';
                e.currentTarget.style.boxShadow = '0 20px 25px -5px rgba(0, 173, 181, 0.3)';
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.transform = 'scale(1)';
                e.currentTarget.style.boxShadow = 'none';
              }}
            >
              {/* 新バッジリボン */}
              {userBadge.isNew && (
                <div style={{ position: 'absolute', top: '-0.5rem', right: '-0.5rem', background: 'linear-gradient(to right, #00ADB5, #00d4e0)', color: '#222831', fontSize: '0.75rem', fontWeight: 'bold', padding: '0.25rem 0.75rem', borderRadius: '9999px', boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.3)' }}>
                  NEW!
                </div>
              )}

              {/* バッジアイコン */}
              <div className="text-center mb-4">
                <div className="text-6xl mb-3">
                  {userBadge.badge.icon}
                </div>
                <h3 style={{ fontSize: '1.25rem', fontWeight: 'bold', color: '#EEEEEE', marginBottom: '0.25rem' }}>
                  {userBadge.badge.name}
                </h3>
                <p style={{ fontSize: '0.875rem', color: '#EEEEEE', opacity: 0.8 }}>
                  {userBadge.badge.description}
                </p>
              </div>

              {/* 獲得日時 */}
              <div style={{ borderTop: '1px solid #393E46', paddingTop: '0.75rem', marginTop: '0.75rem' }}>
                <p style={{ fontSize: '0.75rem', color: '#EEEEEE', opacity: 0.6, textAlign: 'center' }}>
                  獲得日時
                </p>
                <p style={{ fontSize: '0.875rem', color: '#00ADB5', textAlign: 'center', fontWeight: 500 }}>
                  {new Date(userBadge.earnedAt).toLocaleDateString('ja-JP', {
                    year: 'numeric',
                    month: 'long',
                    day: 'numeric',
                  })}
                </p>
              </div>

              {/* バッジタイプ */}
              <div className="mt-2 text-center">
                <span style={{ display: 'inline-block', backgroundColor: '#393E46', color: '#00ADB5', fontSize: '0.75rem', padding: '0.25rem 0.75rem', borderRadius: '9999px' }}>
                  {getBadgeTypeLabel(userBadge.badge.badgeType)}
                </span>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* バッジ統計 */}
      {badges.length > 0 && (
        <div style={{ background: 'linear-gradient(to bottom right, #393E46, #222831)', borderRadius: '0.75rem', padding: '1.5rem', border: '1px solid #00ADB5' }}>
          <h3 style={{ fontSize: '1.25rem', fontWeight: 'bold', color: '#EEEEEE', marginBottom: '1rem' }}>
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
    <div style={{ backgroundColor: '#222831', borderRadius: '0.5rem', padding: '1rem', textAlign: 'center', border: '1px solid #393E46' }}>
      <div className="text-3xl mb-2">{icon}</div>
      <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#00ADB5' }}>{value}</div>
      <div style={{ fontSize: '0.75rem', color: '#EEEEEE', opacity: 0.8 }}>{label}</div>
    </div>
  );
}
