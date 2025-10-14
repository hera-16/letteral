package com.chatapp.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.chatapp.model.ChallengeCompletion;
import com.chatapp.model.DailyChallenge;
import com.chatapp.model.User;

/**
 * チャレンジ達成記録リポジトリ
 */
@Repository
public interface ChallengeCompletionRepository extends JpaRepository<ChallengeCompletion, Long> {
    
    /**
     * ユーザーの達成記録を取得
     */
    List<ChallengeCompletion> findByUserOrderByCompletedAtDesc(User user);
    
    /**
     * ユーザーIDで達成記録を取得
     */
    List<ChallengeCompletion> findByUserIdOrderByCompletedAtDesc(Long userId);
    
    /**
     * チャレンジの達成記録を取得
     */
    List<ChallengeCompletion> findByChallengeOrderByCompletedAtDesc(DailyChallenge challenge);
    
    /**
     * ユーザーが特定のチャレンジを達成したか確認
     */
    boolean existsByUserAndChallenge(User user, DailyChallenge challenge);
    
    /**
     * ユーザーが今日達成したチャレンジを取得
     */
    @Query("SELECT cc FROM ChallengeCompletion cc WHERE cc.user.id = :userId AND cc.completedAt >= :startOfDay")
    List<ChallengeCompletion> findTodayCompletions(@Param("userId") Long userId, @Param("startOfDay") LocalDateTime startOfDay);
    
    /**
     * ユーザーが期間内に達成したチャレンジ数を取得
     */
    @Query("SELECT COUNT(cc) FROM ChallengeCompletion cc WHERE cc.user.id = :userId AND cc.completedAt BETWEEN :start AND :end")
    Long countCompletionsBetween(@Param("userId") Long userId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    /**
     * 最近の達成記録を取得（全ユーザー）
     */
    List<ChallengeCompletion> findTop10ByOrderByCompletedAtDesc();
}
