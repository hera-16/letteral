'use client';

import { useEffect, useMemo, useState } from 'react';
import {
  challengeServiceApi,
  challengeShareService,
  ChallengeCompletionSummary,
  ChallengeShare,
  ChallengeShareReactionType,
  PagedChallengeShares,
} from '@/services/api';

interface ChallengeShareTimelineProps {
  pendingShareChallengeId?: number | null;
  onSharePosted?: () => void;
  onPendingShareConsumed?: () => void;
}

const REACTION_LABELS: Record<ChallengeShareReactionType, string> = {
  ENCOURAGE: '応援',
  EMPATHY: '共感',
  AWESOME: 'すごい！',
};

const MOOD_OPTIONS = [
  { value: 'PROUD', label: '誇らしい', emoji: '🌟' },
  { value: 'GRATEFUL', label: '感謝', emoji: '🙏' },
  { value: 'CALM', label: '落ち着き', emoji: '😌' },
  { value: 'ENERGIZED', label: '元気', emoji: '⚡' },
];

const PAGE_SIZE = 10;

export default function ChallengeShareTimeline({
  pendingShareChallengeId,
  onSharePosted,
  onPendingShareConsumed,
}: ChallengeShareTimelineProps) {
  const [shares, setShares] = useState<ChallengeShare[]>([]);
  const [loading, setLoading] = useState(false);
  const [loadingMore, setLoadingMore] = useState(false);
  const [hasNext, setHasNext] = useState(false);
  const [page, setPage] = useState(0);
  const [completions, setCompletions] = useState<ChallengeCompletionSummary[]>([]);
  const [composerChallengeId, setComposerChallengeId] = useState<number | ''>('');
  const [comment, setComment] = useState('');
  const [mood, setMood] = useState<string>('');
  const [composerError, setComposerError] = useState<string | null>(null);
  const [composerSuccess, setComposerSuccess] = useState<string | null>(null);
  const [submittingShare, setSubmittingShare] = useState(false);

  useEffect(() => {
    loadInitialData();
  }, []);

  useEffect(() => {
    if (pendingShareChallengeId) {
      setComposerChallengeId(pendingShareChallengeId);
      const completion = completions.find((c) => c.challenge.id === pendingShareChallengeId);
      if (completion?.note) {
        setComment(completion.note);
      }
      onPendingShareConsumed?.();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [pendingShareChallengeId]);

  const selectedCompletion = useMemo(() => {
    if (!composerChallengeId) return null;
    return completions.find((c) => c.challenge.id === composerChallengeId) || null;
  }, [composerChallengeId, completions]);

  useEffect(() => {
    if (!comment && selectedCompletion?.note) {
      setComment(selectedCompletion.note);
    }
  }, [selectedCompletion, comment]);

  const loadInitialData = async () => {
    try {
      setLoading(true);
      await Promise.all([loadCompletions(), loadShares(0, true)]);
      await challengeShareService.markRead();
    } catch (error) {
      console.error('Failed to load timeline:', error);
    } finally {
      setLoading(false);
    }
  };

  const loadCompletions = async () => {
    try {
      const history = await challengeServiceApi.getHistory();
      setCompletions(history.slice(0, 20));
    } catch (error) {
      console.error('Failed to load completion history:', error);
    }
  };

  const loadShares = async (targetPage: number, replace = false) => {
    const setter = replace ? setLoading : setLoadingMore;
    setter(true);
    try {
      const response: PagedChallengeShares = await challengeShareService.getTimeline(targetPage, PAGE_SIZE);
      setHasNext(response.hasNext);
      setPage(response.page);
      setShares((prev) => (replace ? response.shares : [...prev, ...response.shares]));
    } catch (error) {
      console.error('Failed to load shares:', error);
    } finally {
      setter(false);
    }
  };

  const handleSubmitShare = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!composerChallengeId) {
      setComposerError('共有するチャレンジを選んでください');
      return;
    }
    try {
      setSubmittingShare(true);
      setComposerError(null);
      const payload = {
        challengeId: composerChallengeId,
        comment: comment.trim() || 'チャレンジを達成しました！',
        mood: mood || undefined,
      };
      const newShare = await challengeShareService.createShare(payload);
      setShares((prev) => [newShare, ...prev]);
      setComposerSuccess('タイムラインに共有しました！');
      setTimeout(() => setComposerSuccess(null), 3000);
      setComment('');
      setMood('');
      onSharePosted?.();
    } catch (error: any) {
      console.error('Failed to share completion:', error);
      const message = error.response?.data?.message || '共有に失敗しました。時間を空けて再試行してください。';
      setComposerError(message);
    } finally {
      setSubmittingShare(false);
    }
  };

  const handleToggleReaction = async (share: ChallengeShare, reaction: ChallengeShareReactionType) => {
    try {
      const updated =
        share.userReaction === reaction
          ? await challengeShareService.removeReaction(share.id, reaction)
          : await challengeShareService.addReaction(share.id, reaction);
      setShares((prev) => prev.map((item) => (item.id === updated.id ? updated : item)));
    } catch (error) {
      console.error('Failed to toggle reaction:', error);
    }
  };

  const handleLoadMore = () => {
    if (hasNext && !loadingMore) {
      loadShares(page + 1, false);
    }
  };

  return (
    <div className="space-y-6">
      <section className="rounded-lg shadow p-6" style={{ backgroundColor: '#393E46' }}>
        <h2 className="text-2xl font-bold mb-4" style={{ color: '#EEEEEE' }}>チャレンジを共有</h2>
        <form onSubmit={handleSubmitShare} className="space-y-4">
          <div>
            <label className="block text-sm font-semibold mb-2" style={{ color: '#EEEEEE' }}>共有するチャレンジ</label>
            <select
              value={composerChallengeId}
              onChange={(e) => setComposerChallengeId(Number(e.target.value) || '')}
              className="w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2"
              style={{ backgroundColor: '#222831', border: '1px solid #00ADB5', color: '#EEEEEE' }}
            >
              <option value="">達成したチャレンジを選択</option>
              {completions.map((completion) => (
                <option key={completion.id} value={completion.challenge.id}>
                  {completion.challenge.title}（{new Date(completion.completedAt).toLocaleString()}）
                </option>
              ))}
            </select>
            {completions.length === 0 && (
              <p className="mt-2 text-sm" style={{ color: '#EEEEEE' }}>まずはデイリーチャレンジを達成してみましょう！</p>
            )}
          </div>

          <div>
            <label className="block text-sm font-semibold mb-2" style={{ color: '#EEEEEE' }}>ひとことメッセージ</label>
            <textarea
              value={comment}
              onChange={(e) => setComment(e.target.value)}
              rows={3}
              placeholder="今日の気づきや感謝の気持ちを書いてみましょう"
              className="w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2"
              style={{ backgroundColor: '#222831', border: '1px solid #00ADB5', color: '#EEEEEE' }}
            />
          </div>

          <div>
            <span className="block text-sm font-semibold mb-2" style={{ color: '#EEEEEE' }}>今の気分</span>
            <div className="flex flex-wrap gap-2">
              {MOOD_OPTIONS.map((option) => (
                <button
                  type="button"
                  key={option.value}
                  onClick={() => setMood((prev) => (prev === option.value ? '' : option.value))}
                  className="px-3 py-2 rounded-full border transition-all"
                  style={{
                    backgroundColor: mood === option.value ? '#00ADB5' : '#222831',
                    color: '#EEEEEE',
                    borderColor: mood === option.value ? '#00ADB5' : '#393E46'
                  }}
                >
                  <span className="mr-1">{option.emoji}</span>
                  {option.label}
                </button>
              ))}
            </div>
          </div>

          {composerError && (
            <div className="p-3 border rounded text-sm" style={{ backgroundColor: '#393E46', borderColor: '#00ADB5', color: '#EEEEEE' }}>
              {composerError}
            </div>
          )}
          {composerSuccess && (
            <div className="p-3 border rounded text-sm" style={{ backgroundColor: '#00ADB5', borderColor: '#00ADB5', color: '#EEEEEE' }}>
              {composerSuccess}
            </div>
          )}

          <div className="flex justify-end">
            <button
              type="submit"
              disabled={submittingShare || !composerChallengeId}
              className="px-6 py-2 rounded-lg font-semibold shadow transition-opacity disabled:opacity-50 disabled:cursor-not-allowed"
              style={{ backgroundColor: '#00ADB5', color: '#EEEEEE' }}
              onMouseEnter={(e) => !submittingShare && composerChallengeId && (e.currentTarget.style.opacity = '0.8')}
              onMouseLeave={(e) => e.currentTarget.style.opacity = '1'}
            >
              {submittingShare ? '共有中...' : 'タイムラインに投稿'}
            </button>
          </div>
        </form>
      </section>

      <section className="space-y-4">
        <div className="flex items-center justify-between">
          <h2 className="text-xl font-bold" style={{ color: '#EEEEEE' }}>みんなの達成</h2>
        </div>

        {loading && shares.length === 0 ? (
          <div className="p-6 rounded-lg shadow text-center" style={{ backgroundColor: '#393E46', color: '#EEEEEE' }}>読み込み中...</div>
        ) : shares.length === 0 ? (
          <div className="p-6 rounded-lg shadow text-center" style={{ backgroundColor: '#393E46', color: '#EEEEEE' }}>
            まだ共有がありません。最初の一歩を踏み出してみましょう！
          </div>
        ) : (
          <div className="space-y-4">
            {shares.map((share) => (
              <article key={share.id} className="rounded-lg shadow p-5" style={{ backgroundColor: '#393E46' }}>
                <div className="flex items-start justify-between gap-4">
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="text-lg">🌸</span>
                      <div>
                        <p className="font-semibold" style={{ color: '#EEEEEE' }}>
                          {share.user.displayName || share.user.username}
                        </p>
                        <p className="text-sm" style={{ color: '#00ADB5' }}>
                          {new Date(share.sharedAt).toLocaleString()}
                        </p>
                      </div>
                    </div>
                    <div className="mt-3">
                      <p className="whitespace-pre-wrap leading-relaxed" style={{ color: '#EEEEEE' }}>{share.comment}</p>
                      {share.mood && (
                        <span className="inline-flex items-center mt-3 px-3 py-1 text-sm font-medium rounded-full" style={{ backgroundColor: '#00ADB5', color: '#EEEEEE' }}>
                          🌱 気分: {share.mood}
                        </span>
                      )}
                    </div>
                  </div>
                  <div className="px-3 py-2 rounded-lg text-sm text-center min-w-[140px]" style={{ backgroundColor: '#222831', color: '#00ADB5' }}>
                    <div className="font-semibold">{share.challenge.title}</div>
                    <div className="text-xs mt-1" style={{ color: '#EEEEEE' }}>
                      {share.challenge.challengeType}
                    </div>
                    <div className="text-xs mt-1">+{share.challenge.points}pt</div>
                  </div>
                </div>

                <div className="mt-4 flex gap-3">
                  {(Object.keys(REACTION_LABELS) as ChallengeShareReactionType[]).map((type) => {
                    const isActive = share.userReaction === type;
                    const count = share.reactions?.[type] || 0;
                    return (
                      <button
                        key={type}
                        type="button"
                        onClick={() => handleToggleReaction(share, type)}
                        className="flex items-center gap-1 px-3 py-1.5 rounded-full border transition-all"
                        style={{
                          backgroundColor: isActive ? '#00ADB5' : '#222831',
                          borderColor: isActive ? '#00ADB5' : '#393E46',
                          color: '#EEEEEE'
                        }}
                      >
                        <ReactionEmoji type={type} />
                        <span className="text-sm font-medium">{REACTION_LABELS[type]}</span>
                        <span className="text-xs">{count}</span>
                      </button>
                    );
                  })}
                </div>
              </article>
            ))}

            {hasNext && (
              <div className="text-center">
                <button
                  onClick={handleLoadMore}
                  disabled={loadingMore}
                  className="px-6 py-2 rounded-lg transition-opacity disabled:opacity-50"
                  style={{ backgroundColor: '#393E46', color: '#EEEEEE' }}
                  onMouseEnter={(e) => !loadingMore && (e.currentTarget.style.opacity = '0.8')}
                  onMouseLeave={(e) => e.currentTarget.style.opacity = '1'}
                >
                  {loadingMore ? '読み込み中...' : 'もっと見る'}
                </button>
              </div>
            )}
          </div>
        )}
      </section>
    </div>
  );
}

function ReactionEmoji({ type }: { type: ChallengeShareReactionType }) {
  switch (type) {
    case 'ENCOURAGE':
      return <span>💪</span>;
    case 'EMPATHY':
      return <span>🤝</span>;
    case 'AWESOME':
      return <span>🔥</span>;
    default:
      return null;
  }
}
