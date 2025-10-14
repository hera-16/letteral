package com.chatapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.chatapp.model.DailyChallenge;
import com.chatapp.model.DailyChallenge.ChallengeType;
import com.chatapp.model.DailyChallenge.DifficultyLevel;

/**
 * デイリーチャレンジリポジトリ
 */
@Repository
public interface DailyChallengeRepository extends JpaRepository<DailyChallenge, Long> {
    
    /**
     * アクティブなチャレンジを取得
     */
    List<DailyChallenge> findByIsActiveTrue();
    
    /**
     * タイプ別にアクティブなチャレンジを取得
     */
    List<DailyChallenge> findByChallengeTypeAndIsActiveTrue(ChallengeType challengeType);
    
    /**
     * 難易度別にアクティブなチャレンジを取得
     */
    List<DailyChallenge> findByDifficultyLevelAndIsActiveTrue(DifficultyLevel difficultyLevel);
    
    /**
     * ランダムにアクティブなチャレンジを1つ取得
     */
    @Query(value = "SELECT * FROM daily_challenges WHERE is_active = true ORDER BY RAND() LIMIT 1", nativeQuery = true)
    DailyChallenge findRandomActiveChallenge();
    
    /**
     * タイプ別にランダムなチャレンジを3つ取得
     */
    @Query(value = "SELECT * FROM daily_challenges WHERE challenge_type = ?1 AND is_active = true ORDER BY RAND() LIMIT 3", nativeQuery = true)
    List<DailyChallenge> findRandomChallengesByType(String challengeType);
}
