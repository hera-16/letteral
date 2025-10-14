package com.chatapp.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chatapp.model.ChallengeCompletion;
import com.chatapp.model.DailyChallenge;
import com.chatapp.model.User;
import com.chatapp.model.UserProgress;
import com.chatapp.repository.ChallengeCompletionRepository;
import com.chatapp.repository.DailyChallengeRepository;
import com.chatapp.repository.UserProgressRepository;
import com.chatapp.repository.UserRepository;

/**
 * デイリーチャレンジのビジネスロジックを提供するサービス
 */
@Service
public class ChallengeService {
    
    private final DailyChallengeRepository dailyChallengeRepository;
    private final UserProgressRepository userProgressRepository;
    private final ChallengeCompletionRepository challengeCompletionRepository;
    private final UserRepository userRepository;
    private final Random random = new Random();
    
    public ChallengeService(
            DailyChallengeRepository dailyChallengeRepository,
            UserProgressRepository userProgressRepository,
            ChallengeCompletionRepository challengeCompletionRepository,
            UserRepository userRepository) {
        this.dailyChallengeRepository = dailyChallengeRepository;
        this.userProgressRepository = userProgressRepository;
        this.challengeCompletionRepository = challengeCompletionRepository;
        this.userRepository = userRepository;
    }
    
    /**
     * 今日のおすすめチャレンジを取得
     * ユーザーがまだ達成していないチャレンジをランダムで3つ返す
     */
    public List<DailyChallenge> getTodayRecommendedChallenges(Long userId) {
        // ユーザーの今日の達成済みチャレンジを取得
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        List<ChallengeCompletion> todayCompletions = challengeCompletionRepository.findTodayCompletions(userId, startOfDay);
        
        // 全アクティブチャレンジを取得
        List<DailyChallenge> allChallenges = dailyChallengeRepository.findByIsActiveTrue();
        
        // 今日達成済みのチャレンジIDを抽出
        List<Long> completedChallengeIds = todayCompletions.stream()
                .map(completion -> completion.getChallenge().getId())
                .toList();
        
        // 未達成のチャレンジのみにフィルタリング
        List<DailyChallenge> availableChallenges = allChallenges.stream()
                .filter(challenge -> !completedChallengeIds.contains(challenge.getId()))
                .toList();
        
        // ランダムに3つ選択（3つ未満の場合は全て返す）
        if (availableChallenges.size() <= 3) {
            return availableChallenges;
        }
        
        return availableChallenges.stream()
                .sorted((a, b) -> random.nextInt(3) - 1)
                .limit(3)
                .toList();
    }
    
    /**
     * チャレンジタイプ別のおすすめチャレンジを取得
     */
    public List<DailyChallenge> getChallengesByType(DailyChallenge.ChallengeType type) {
        return dailyChallengeRepository.findRandomChallengesByType(type.name());
    }
    
    /**
     * チャレンジを達成する
     * ポイントを加算し、ストリークを更新し、花レベルを計算する
     */
    @Transactional
    public ChallengeCompletion completeChallenge(Long userId, Long challengeId, String note) {
        // ユーザーとチャレンジを取得
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません"));
        
        DailyChallenge challenge = dailyChallengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("チャレンジが見つかりません"));
        
        // 今日既に達成済みかチェック
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        boolean alreadyCompleted = challengeCompletionRepository.existsByUserAndChallenge(user, challenge);
        
        if (alreadyCompleted) {
            List<ChallengeCompletion> todayCompletions = challengeCompletionRepository.findTodayCompletions(userId, startOfDay);
            boolean completedToday = todayCompletions.stream()
                    .anyMatch(completion -> completion.getChallenge().getId().equals(challengeId));
            
            if (completedToday) {
                throw new RuntimeException("このチャレンジは今日既に達成済みです");
            }
        }
        
        // ユーザー進捗を取得または作成
        UserProgress progress = userProgressRepository.findByUser(user)
                .orElseGet(() -> {
                    UserProgress newProgress = new UserProgress();
                    newProgress.setUser(user);
                    newProgress.setTotalPoints(0);
                    newProgress.setFlowerLevel(1);
                    newProgress.setCurrentStreak(0);
                    newProgress.setLongestStreak(0);
                    return newProgress;
                });
        
        // ポイントを加算（花レベルは自動的に更新される）
        progress.addPoints(challenge.getPoints());
        
        // ストリークを更新
        progress.updateStreak(LocalDate.now());
        
        // 進捗を保存
        userProgressRepository.save(progress);
        
        // チャレンジ達成記録を作成
        ChallengeCompletion completion = new ChallengeCompletion();
        completion.setUser(user);
        completion.setChallenge(challenge);
        completion.setPointsEarned(challenge.getPoints());
        completion.setNote(note);
        
        return challengeCompletionRepository.save(completion);
    }
    
    /**
     * ユーザーの進捗情報を取得
     */
    public UserProgress getUserProgress(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません"));
        
        return userProgressRepository.findByUser(user)
                .orElseGet(() -> {
                    // 進捗がない場合は初期値を返す（保存はしない）
                    UserProgress newProgress = new UserProgress();
                    newProgress.setUser(user);
                    newProgress.setTotalPoints(0);
                    newProgress.setFlowerLevel(1);
                    newProgress.setCurrentStreak(0);
                    newProgress.setLongestStreak(0);
                    return newProgress;
                });
    }
    
    /**
     * ユーザーのチャレンジ達成履歴を取得
     */
    public List<ChallengeCompletion> getUserChallengeHistory(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません"));
        
        return challengeCompletionRepository.findByUserOrderByCompletedAtDesc(user);
    }
    
    /**
     * 今日達成したチャレンジの数を取得
     */
    public int getTodayCompletedCount(Long userId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        return challengeCompletionRepository.findTodayCompletions(userId, startOfDay).size();
    }
    
    /**
     * ランキングを取得（上位10人）
     */
    public List<UserProgress> getTopRanking(int limit) {
        return userProgressRepository.findTopUsers();
    }
    
    /**
     * 特定期間内のチャレンジ達成数をカウント
     */
    public long countCompletionsBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        return challengeCompletionRepository.countCompletionsBetween(userId, startDate, endDate);
    }
}
