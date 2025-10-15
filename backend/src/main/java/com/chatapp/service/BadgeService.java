package com.chatapp.service;

import com.chatapp.model.*;
import com.chatapp.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * バッジ管理サービス
 */
@Service
public class BadgeService {
    
    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final UserProgressRepository userProgressRepository;
    private final ChallengeCompletionRepository challengeCompletionRepository;
    
    public BadgeService(
            BadgeRepository badgeRepository,
            UserBadgeRepository userBadgeRepository,
            UserProgressRepository userProgressRepository,
            ChallengeCompletionRepository challengeCompletionRepository) {
        this.badgeRepository = badgeRepository;
        this.userBadgeRepository = userBadgeRepository;
        this.userProgressRepository = userProgressRepository;
        this.challengeCompletionRepository = challengeCompletionRepository;
    }
    
    /**
     * ユーザーの獲得バッジ一覧を取得
     */
    public List<UserBadge> getUserBadges(User user) {
        return userBadgeRepository.findByUserOrderByEarnedAtDesc(user);
    }
    
    /**
     * 新しく獲得したバッジを取得（未読）
     */
    public List<UserBadge> getNewBadges(User user) {
        return userBadgeRepository.findNewBadgesByUser(user);
    }
    
    /**
     * バッジを既読にする
     */
    @Transactional
    public void markBadgesAsRead(User user) {
        List<UserBadge> newBadges = userBadgeRepository.findNewBadgesByUser(user);
        for (UserBadge userBadge : newBadges) {
            userBadge.setIsNew(false);
            userBadgeRepository.save(userBadge);
        }
    }
    
    /**
     * バッジ獲得条件をチェックして、新しいバッジを付与
     */
    @Transactional
    public List<UserBadge> checkAndAwardBadges(User user) {
        List<UserBadge> newlyEarnedBadges = new ArrayList<>();
        
        // ユーザー進捗を取得
        UserProgress progress = userProgressRepository.findByUser(user).orElse(null);
        if (progress == null) {
            return newlyEarnedBadges;
        }
        
        // 総達成数を取得
        long totalCompletions = challengeCompletionRepository.countByUser(user);
        
        // 各バッジ条件をチェック
        
        // 初めての一歩（1回達成）
        if (totalCompletions >= 1) {
            newlyEarnedBadges.addAll(awardBadgeIfNotExists(user, "FIRST_STEP"));
        }
        
        // ストリーク系
        if (progress.getCurrentStreak() >= 3) {
            newlyEarnedBadges.addAll(awardBadgeIfNotExists(user, "STREAK_3"));
        }
        if (progress.getCurrentStreak() >= 7) {
            newlyEarnedBadges.addAll(awardBadgeIfNotExists(user, "STREAK_7"));
        }
        
        // 総達成数
        if (totalCompletions >= 10) {
            newlyEarnedBadges.addAll(awardBadgeIfNotExists(user, "TOTAL_10"));
        }
        if (totalCompletions >= 30) {
            newlyEarnedBadges.addAll(awardBadgeIfNotExists(user, "TOTAL_30"));
        }
        if (totalCompletions >= 50) {
            newlyEarnedBadges.addAll(awardBadgeIfNotExists(user, "TOTAL_50"));
        }
        
        // カテゴリ別達成数
        long gratitudeCount = challengeCompletionRepository.countByChallengeType(user, DailyChallenge.ChallengeType.GRATITUDE);
        if (gratitudeCount >= 10) {
            newlyEarnedBadges.addAll(awardBadgeIfNotExists(user, "GRATITUDE_10"));
        }
        
        long kindnessCount = challengeCompletionRepository.countByChallengeType(user, DailyChallenge.ChallengeType.KINDNESS);
        if (kindnessCount >= 10) {
            newlyEarnedBadges.addAll(awardBadgeIfNotExists(user, "KINDNESS_10"));
        }
        
        long selfCareCount = challengeCompletionRepository.countByChallengeType(user, DailyChallenge.ChallengeType.SELF_CARE);
        if (selfCareCount >= 10) {
            newlyEarnedBadges.addAll(awardBadgeIfNotExists(user, "SELF_CARE_10"));
        }
        
        long creativityCount = challengeCompletionRepository.countByChallengeType(user, DailyChallenge.ChallengeType.CREATIVITY);
        if (creativityCount >= 10) {
            newlyEarnedBadges.addAll(awardBadgeIfNotExists(user, "CREATIVITY_10"));
        }
        
        long connectionCount = challengeCompletionRepository.countByChallengeType(user, DailyChallenge.ChallengeType.CONNECTION);
        if (connectionCount >= 10) {
            newlyEarnedBadges.addAll(awardBadgeIfNotExists(user, "CONNECTION_10"));
        }
        
        // 花レベル
        if (progress.getFlowerLevel() >= 3) {
            newlyEarnedBadges.addAll(awardBadgeIfNotExists(user, "LEVEL_3"));
        }
        if (progress.getFlowerLevel() >= 5) {
            newlyEarnedBadges.addAll(awardBadgeIfNotExists(user, "LEVEL_5"));
        }
        if (progress.getFlowerLevel() >= 7) {
            newlyEarnedBadges.addAll(awardBadgeIfNotExists(user, "LEVEL_7"));
        }
        if (progress.getFlowerLevel() >= 10) {
            newlyEarnedBadges.addAll(awardBadgeIfNotExists(user, "LEVEL_10"));
        }
        
        return newlyEarnedBadges;
    }
    
    /**
     * バッジがまだ獲得されていなければ付与
     */
    private List<UserBadge> awardBadgeIfNotExists(User user, String badgeType) {
        List<UserBadge> result = new ArrayList<>();
        
        // 既に獲得済みかチェック
        if (userBadgeRepository.existsByUserAndBadge_BadgeType(user, badgeType)) {
            return result;
        }
        
        // バッジを取得
        Badge badge = badgeRepository.findByBadgeType(badgeType).orElse(null);
        if (badge == null) {
            return result;
        }
        
        // ユーザーバッジを作成
        UserBadge userBadge = new UserBadge();
        userBadge.setUser(user);
        userBadge.setBadge(badge);
        userBadge.setIsNew(true);
        
        UserBadge saved = userBadgeRepository.save(userBadge);
        result.add(saved);
        
        return result;
    }
}
