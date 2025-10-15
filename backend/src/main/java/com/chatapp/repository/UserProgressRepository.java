package com.chatapp.repository;

import com.chatapp.model.User;
import com.chatapp.model.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ユーザー進捗リポジトリ
 */
@Repository
public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {
    
    /**
     * ユーザーIDで進捗を取得
     */
    Optional<UserProgress> findByUserId(Long userId);
    
    /**
     * ユーザーで進捗を取得
     */
    Optional<UserProgress> findByUser(User user);
    
    /**
     * 花レベル別にユーザー進捗を取得
     */
    List<UserProgress> findByFlowerLevel(Integer flowerLevel);
    
    /**
     * 花レベル順にユーザー進捗を取得（ランキング）
     */
    List<UserProgress> findAllByOrderByFlowerLevelDescTotalPointsDesc();
    
    /**
     * トップN人のユーザー進捗を取得
     */
    @Query("SELECT up FROM UserProgress up ORDER BY up.flowerLevel DESC, up.totalPoints DESC")
    List<UserProgress> findTopUsers();
    
    /**
     * 連続達成日数が指定以上のユーザーを取得
     */
    List<UserProgress> findByCurrentStreakGreaterThanEqual(Integer minStreak);
}
