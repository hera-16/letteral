'use client';

import { useState, useEffect } from 'react';
import api, { badgeService, UserBadge } from '@/services/api';
import BadgeNotificationModal from './BadgeNotificationModal';

interface DailyChallenge {
  id: number;
  title: string;
  description: string;
  points: number;
  challengeType: string;
  difficultyLevel: string;
}

interface UserProgress {
  id: number;
  totalPoints: number;
  flowerLevel: number;
  currentStreak: number;
  longestStreak: number;
}

interface ProgressData {
  progress: UserProgress;
  flowerEmoji: string;
  todayCompletedCount: number;
  pointsToNextLevel: number;
  progressPercentage: number;
  dailyLimit: number;
}

interface DailyChallengesProps {
  onRequestShare?: (challengeId: number) => void;
}

interface CompletionSummary {
  challengeId: number;
  challengeTitle: string;
}

export default function DailyChallenges({ onRequestShare }: DailyChallengesProps) {
  const [challenges, setChallenges] = useState<DailyChallenge[]>([]);
  const [progressData, setProgressData] = useState<ProgressData | null>(null);
  const [loading, setLoading] = useState(true);
  const [completingId, setCompletingId] = useState<number | null>(null);
  const [showSuccess, setShowSuccess] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [showConfetti, setShowConfetti] = useState(false);
  const [encouragementMessage, setEncouragementMessage] = useState('');
  const [newBadges, setNewBadges] = useState<UserBadge[]>([]);
  const [showBadgeModal, setShowBadgeModal] = useState(false);
  const [completionError, setCompletionError] = useState<string | null>(null);
  const [lastCompletion, setLastCompletion] = useState<CompletionSummary | null>(null);

  const hasReachedDailyLimit = progressData ? progressData.todayCompletedCount >= progressData.dailyLimit : false;

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      setError(null);
      
      // トークンの存在確認
      const token = localStorage.getItem('token');
      console.log('トークン確認:', {
        hasToken: !!token,
        tokenLength: token?.length,
        tokenPrefix: token?.substring(0, 30)
      });
      
      console.log('チャレンジデータを読み込み中...');
      const [challengesRes, progressRes] = await Promise.all([
        api.get('/challenges/today'),
        api.get('/challenges/progress')
      ]);

      console.log('チャレンジレスポンス:', challengesRes);
      console.log('チャレンジデータ:', challengesRes.data);
      console.log('チャレンジ配列:', challengesRes.data.data);
      console.log('進捗レスポンス:', progressRes);

      if (challengesRes.data.success) {
        console.log('チャレンジ数:', challengesRes.data.data.length);
        setChallenges(challengesRes.data.data);
      } else {
        console.error('チャレンジ取得失敗:', challengesRes.data);
      }

      if (progressRes.data.success) {
        setProgressData(progressRes.data.data);
        setCompletionError(null);
      }
    } catch (error: any) {
      console.error('データの読み込みに失敗しました:', error);
      console.error('エラーレスポンス:', error.response);
      console.error('エラーステータス:', error.response?.status);
      console.error('エラーデータ:', error.response?.data);
      
      const status = error.response?.status;
      if (status === 404) {
        setError('APIエンドポイントが見つかりません。バックエンドを再起動してください。');
      } else if (status === 401) {
        setError('認証エラー。再度ログインしてください。');
      } else {
        setError('データの読み込みに失敗しました: ' + (error.response?.data?.message || error.message));
      }
    } finally {
      setLoading(false);
    }
  };

  const completeChallenge = async (challengeId: number) => {
    try {
      if (hasReachedDailyLimit) {
        setCompletionError('今日達成できるデイリーチャレンジは3つまでです。明日またチャレンジしましょう！');
        return;
      }
      setCompletingId(challengeId);
      const response = await api.post(`/challenges/${challengeId}/complete`, {
        note: ''
      });

      if (response.data.success) {
        const data = response.data.data;
        setCompletionError(null);
        
        // 励ましメッセージをランダム選択
        const encouragements = [
          '素晴らしい！一歩前進です！ 🌟',
          'よくできました！自分を誇りに思ってください！ ✨',
          'すごい！着実に成長していますね！ 🌱',
          '達成おめでとう！その調子です！ 🎉',
          '頑張りましたね！小さな成功の積み重ねが大きな変化に！ 💪',
          'やった！自分を信じる力が育っています！ 🌈',
          '素敵です！あなたの努力が花開いています！ 🌸',
          'お疲れ様！今日も一つ成長できましたね！ ⭐',
        ];
        const randomEncouragement = encouragements[Math.floor(Math.random() * encouragements.length)];
        
        setSuccessMessage(data.message + (data.levelUpMessage ? ' ' + data.levelUpMessage : ''));
        setEncouragementMessage(randomEncouragement);
        setShowSuccess(true);
        setShowConfetti(true);
        
        // 3秒後にメッセージを消す
        setTimeout(() => {
          setShowSuccess(false);
          setShowConfetti(false);
        }, 5000);

        // レスポンスから新規バッジを取得
        if (data.newBadges && data.newBadges.length > 0) {
          setNewBadges(data.newBadges);
          setShowBadgeModal(true);
        }

        if (data.completion && data.completion.challenge) {
          setLastCompletion({
            challengeId: data.completion.challenge.id,
            challengeTitle: data.completion.challenge.title,
          });
        } else {
          setLastCompletion(null);
        }

        // データを再読み込み
        await loadData();
      }
    } catch (error: any) {
      const message = error.response?.data?.message || 'チャレンジの達成に失敗しました';
      setCompletionError(message);
      setLastCompletion(null);
    } finally {
      setCompletingId(null);
    }
  };

  const handleCloseBadgeModal = async () => {
    setShowBadgeModal(false);
    setNewBadges([]);
    // バッジを既読にする
    try {
      await badgeService.markBadgesAsRead();
    } catch (error) {
      console.error('バッジの既読化に失敗しました:', error);
    }
  };

  const handleRequestShare = () => {
    if (lastCompletion) {
      onRequestShare?.(lastCompletion.challengeId);
      setLastCompletion(null);
    }
  };

  const getDifficultyColor = (level: string) => {
    switch (level) {
      case 'EASY': return 'bg-green-100 text-green-800';
      case 'MEDIUM': return 'bg-yellow-100 text-yellow-800';
      case 'HARD': return 'bg-red-100 text-red-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  const getDifficultyLabel = (level: string) => {
    switch (level) {
      case 'EASY': return '簡単';
      case 'MEDIUM': return '普通';
      case 'HARD': return '難しい';
      default: return level;
    }
  };

  const getChallengeTypeLabel = (type: string) => {
    switch (type) {
      case 'GRATITUDE': return '感謝';
      case 'KINDNESS': return '優しさ';
      case 'SELF_CARE': return 'セルフケア';
      case 'CREATIVITY': return '創造性';
      case 'CONNECTION': return 'つながり';
      default: return type;
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <div className="text-gray-500">読み込み中...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="max-w-4xl mx-auto p-4">
        <div className="p-6 bg-red-50 border border-red-200 rounded-lg">
          <h3 className="text-lg font-bold text-red-800 mb-2">エラーが発生しました</h3>
          <p className="text-red-700 mb-4">{error}</p>
          <button
            onClick={loadData}
            className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700"
          >
            再試行
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto p-4">
      {/* 紙吹雪エフェクト */}
      {showConfetti && (
        <div className="fixed inset-0 pointer-events-none z-50 overflow-hidden">
          {[...Array(50)].map((_, i) => (
            <div
              key={i}
              className="absolute animate-confetti"
              style={{
                left: `${Math.random() * 100}%`,
                top: '-10%',
                animationDelay: `${Math.random() * 0.5}s`,
                animationDuration: `${2 + Math.random() * 2}s`,
              }}
            >
              {['🌸', '✨', '⭐', '🌟', '💫', '🎉'][Math.floor(Math.random() * 6)]}
            </div>
          ))}
        </div>
      )}

      {/* 成功メッセージ */}
      {showSuccess && (
        <div className="mb-4 p-6 bg-gradient-to-r from-green-50 to-blue-50 border-2 border-green-400 rounded-xl shadow-lg animate-bounce-in">
          <div className="flex items-center gap-3 mb-3">
            <span className="text-4xl animate-spin-slow">🎉</span>
            <div className="flex-1">
              <h3 className="text-xl font-bold text-green-800 mb-1">チャレンジ達成！</h3>
              <p className="text-green-700">{successMessage}</p>
            </div>
          </div>
          <div className="mt-3 p-3 bg-white bg-opacity-70 rounded-lg">
            <p className="text-lg font-semibold text-purple-700 text-center">
              {encouragementMessage}
            </p>
          </div>
          {lastCompletion && onRequestShare && (
            <div className="mt-4 flex justify-center">
              <button
                onClick={handleRequestShare}
                className="px-5 py-2 bg-gradient-to-r from-purple-500 to-pink-500 text-white font-semibold rounded-full shadow hover:from-purple-600 hover:to-pink-600 transition-all"
              >
                🌟 タイムラインに共有する
              </button>
            </div>
          )}
        </div>
      )}

      {lastCompletion && onRequestShare && (
        <div className="mb-6 p-5 bg-purple-50 border border-purple-200 rounded-xl shadow-sm">
          <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-3">
            <div>
              <h3 className="text-lg font-bold text-purple-800">直近の達成をみんなとシェアしよう！</h3>
              <p className="text-purple-700 text-sm">
                「{lastCompletion.challengeTitle}」をタイムラインに投稿して仲間からの応援を受け取ろう。
              </p>
            </div>
            <button
              onClick={handleRequestShare}
              className="self-start md:self-auto px-5 py-2 bg-white text-purple-700 border border-purple-300 rounded-full font-semibold shadow-sm hover:shadow transition-all"
            >
              🌟 タイムラインに共有する
            </button>
          </div>
        </div>
      )}

      {/* 進捗情報 */}
      {progressData && (
        <div className="mb-6 p-6 bg-gradient-to-r from-purple-50 to-pink-50 rounded-lg shadow-md">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-2xl font-bold text-gray-800">あなたの花の成長</h2>
            <div className="text-6xl hover:scale-110 transition-transform cursor-pointer animate-pulse-glow">
              {progressData.flowerEmoji}
            </div>
          </div>
          
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-4">
            <div className="text-center p-3 bg-white rounded-lg">
              <div className="text-sm text-gray-600">レベル</div>
              <div className="text-2xl font-bold text-purple-600">
                {progressData.progress.flowerLevel}
              </div>
            </div>
            <div className="text-center p-3 bg-white rounded-lg">
              <div className="text-sm text-gray-600">合計ポイント</div>
              <div className="text-2xl font-bold text-blue-600">
                {progressData.progress.totalPoints}
              </div>
            </div>
            <div className="text-center p-3 bg-white rounded-lg">
              <div className="text-sm text-gray-600">連続達成</div>
              <div className="text-2xl font-bold text-orange-600">
                {progressData.progress.currentStreak}日
              </div>
            </div>
            <div className="text-center p-3 bg-white rounded-lg">
              <div className="text-sm text-gray-600">今日の達成</div>
              <div className="text-2xl font-bold text-green-600">
                {progressData.todayCompletedCount} / {progressData.dailyLimit}個
              </div>
            </div>
          </div>

          {hasReachedDailyLimit && (
            <div className="mt-2 text-sm font-semibold text-purple-700">
              素晴らしい！今日は3件のチャレンジをすべてやり遂げました。
            </div>
          )}

          {/* プログレスバー */}
          <div className="mb-2">
            <div className="flex justify-between text-sm text-gray-600 mb-1">
              <span>次のレベルまで</span>
              <span>{progressData.pointsToNextLevel}ポイント</span>
            </div>
            <div className="w-full bg-gray-200 rounded-full h-3 overflow-hidden">
              <div 
                className="h-full bg-gradient-to-r from-purple-500 to-pink-500 transition-all duration-500 relative overflow-hidden"
                style={{ width: `${progressData.progressPercentage}%` }}
              >
                <div className="absolute inset-0 bg-gradient-to-r from-transparent via-white to-transparent opacity-30 animate-shimmer"></div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* チャレンジリスト */}
      <div className="mb-6">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-xl font-bold text-gray-800">今日のチャレンジ</h2>
          
          {/* デバッグ用リセットボタン（開発環境のみ） */}
          {process.env.NODE_ENV === 'development' && (
            <button
              onClick={async () => {
                if (confirm('達成履歴をリセットしますか？（開発用）')) {
                  try {
                    await api.delete('/challenges/debug/reset-today');
                    alert('リセット完了！ページをリロードしてください。');
                    window.location.reload();
                  } catch (error) {
                    console.error('リセット失敗:', error);
                    alert('リセットに失敗しました。手動でMySQLをリセットしてください。');
                  }
                }
              }}
              className="px-4 py-2 bg-yellow-500 text-white rounded-lg text-sm hover:bg-yellow-600"
            >
              🔧 デバッグ: 今日の達成をリセット
            </button>
          )}
        </div>
        
        {completionError && (
          <div className="mb-4 p-4 bg-red-50 border border-red-200 text-red-700 rounded-lg">
            {completionError}
          </div>
        )}

        {hasReachedDailyLimit && !completionError && (
          <div className="mb-4 p-4 bg-blue-50 border border-blue-200 text-blue-800 rounded-lg">
            今日のデイリーチャレンジは3件までです。よく頑張りました！
          </div>
        )}

        {challenges.length === 0 ? (
          <div className="text-center p-8 bg-gray-50 rounded-lg">
            <p className="text-gray-600 mb-2">🎉 素晴らしい!</p>
            <p className="text-gray-600 mb-4">
              {hasReachedDailyLimit
                ? '今日の上限である3件を達成しました。ゆっくり休んでくださいね！'
                : '今日のチャレンジは全て達成しました!'}
            </p>
            
            {/* デバッグ用: リセットボタン */}
            {process.env.NODE_ENV === 'development' && (
              <button
                onClick={async () => {
                  if (confirm('全てのデータをリセットしますか？（達成記録、バッジ、進捗）')) {
                    try {
                      const response = await api.delete('/debug/reset-all');
                      alert(response.data.data || 'リセット完了！');
                      window.location.reload();
                    } catch (error: any) {
                      console.error('リセット失敗:', error);
                      alert('リセットに失敗しました: ' + (error.response?.data?.message || error.message));
                    }
                  }
                }}
                className="mt-4 px-6 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600"
              >
                🔧 デバッグ: 全てリセット
              </button>
            )}
          </div>
        ) : (
          <div className="space-y-4">
            {challenges.map((challenge) => (
              <div 
                key={challenge.id} 
                className="p-5 bg-white border border-gray-200 rounded-lg shadow-sm hover:shadow-md transition-shadow"
              >
                <div className="flex items-start justify-between mb-3">
                  <div className="flex-1">
                    <div className="flex items-center gap-2 mb-2">
                      <span className={`px-2 py-1 text-xs font-semibold rounded-full ${getDifficultyColor(challenge.difficultyLevel)}`}>
                        {getDifficultyLabel(challenge.difficultyLevel)}
                      </span>
                      <span className="px-2 py-1 text-xs font-semibold rounded-full bg-blue-100 text-blue-800">
                        {getChallengeTypeLabel(challenge.challengeType)}
                      </span>
                      <span className="text-sm text-gray-600">+{challenge.points}pt</span>
                    </div>
                    <h3 className="text-lg font-bold text-gray-800 mb-1">
                      {challenge.title}
                    </h3>
                    <p className="text-gray-600 text-sm">
                      {challenge.description}
                    </p>
                  </div>
                </div>
                
                <button
                  onClick={() => completeChallenge(challenge.id)}
                  disabled={completingId === challenge.id || hasReachedDailyLimit}
                  className="w-full mt-3 px-4 py-2 bg-gradient-to-r from-purple-500 to-pink-500 text-white font-semibold rounded-lg hover:from-purple-600 hover:to-pink-600 disabled:opacity-50 disabled:cursor-not-allowed transition-all"
                >
                  {completingId === challenge.id
                    ? '達成中...'
                    : hasReachedDailyLimit
                      ? '今日の上限に達しました'
                      : '達成!'}
                </button>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* バッジ獲得モーダル */}
      {showBadgeModal && newBadges.length > 0 && (
        <BadgeNotificationModal
          badges={newBadges}
          onClose={handleCloseBadgeModal}
        />
      )}
    </div>
  );
}
