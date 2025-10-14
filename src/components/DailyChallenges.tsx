'use client';

import { useState, useEffect } from 'react';
import api from '@/services/api';

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
}

export default function DailyChallenges() {
  const [challenges, setChallenges] = useState<DailyChallenge[]>([]);
  const [progressData, setProgressData] = useState<ProgressData | null>(null);
  const [loading, setLoading] = useState(true);
  const [completingId, setCompletingId] = useState<number | null>(null);
  const [showSuccess, setShowSuccess] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');
  const [error, setError] = useState<string | null>(null);

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
      console.log('進捗レスポンス:', progressRes);

      if (challengesRes.data.success) {
        setChallenges(challengesRes.data.data);
      }

      if (progressRes.data.success) {
        setProgressData(progressRes.data.data);
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
      setCompletingId(challengeId);
      const response = await api.post(`/challenges/${challengeId}/complete`, {
        note: ''
      });

      if (response.data.success) {
        const data = response.data.data;
        setSuccessMessage(data.message + (data.levelUpMessage ? ' ' + data.levelUpMessage : ''));
        setShowSuccess(true);
        setTimeout(() => setShowSuccess(false), 5000);

        // データを再読み込み
        await loadData();
      }
    } catch (error: any) {
      alert(error.response?.data?.message || 'チャレンジの達成に失敗しました');
    } finally {
      setCompletingId(null);
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
      {/* 成功メッセージ */}
      {showSuccess && (
        <div className="mb-4 p-4 bg-green-100 border border-green-400 text-green-700 rounded-lg animate-fade-in">
          {successMessage}
        </div>
      )}

      {/* 進捗情報 */}
      {progressData && (
        <div className="mb-6 p-6 bg-gradient-to-r from-purple-50 to-pink-50 rounded-lg shadow-md">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-2xl font-bold text-gray-800">あなたの花の成長</h2>
            <div className="text-6xl">{progressData.flowerEmoji}</div>
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
                {progressData.todayCompletedCount}個
              </div>
            </div>
          </div>

          {/* プログレスバー */}
          <div className="mb-2">
            <div className="flex justify-between text-sm text-gray-600 mb-1">
              <span>次のレベルまで</span>
              <span>{progressData.pointsToNextLevel}ポイント</span>
            </div>
            <div className="w-full bg-gray-200 rounded-full h-3 overflow-hidden">
              <div 
                className="h-full bg-gradient-to-r from-purple-500 to-pink-500 transition-all duration-500"
                style={{ width: `${progressData.progressPercentage}%` }}
              ></div>
            </div>
          </div>
        </div>
      )}

      {/* チャレンジリスト */}
      <div className="mb-6">
        <h2 className="text-xl font-bold text-gray-800 mb-4">今日のチャレンジ</h2>
        
        {challenges.length === 0 ? (
          <div className="text-center p-8 bg-gray-50 rounded-lg">
            <p className="text-gray-600 mb-2">🎉 素晴らしい!</p>
            <p className="text-gray-600">今日のチャレンジは全て達成しました!</p>
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
                  disabled={completingId === challenge.id}
                  className="w-full mt-3 px-4 py-2 bg-gradient-to-r from-purple-500 to-pink-500 text-white font-semibold rounded-lg hover:from-purple-600 hover:to-pink-600 disabled:opacity-50 disabled:cursor-not-allowed transition-all"
                >
                  {completingId === challenge.id ? '達成中...' : '達成!'}
                </button>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
