package com.chatapp.repository;

import com.chatapp.model.User;
import com.chatapp.model.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ユーザーバッジリポジトリ
 */
@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {
    
    List<UserBadge> findByUserOrderByEarnedAtDesc(User user);
    
    @Query("SELECT ub FROM UserBadge ub WHERE ub.user = ?1 AND ub.isNew = true ORDER BY ub.earnedAt DESC")
    List<UserBadge> findNewBadgesByUser(User user);
    
    boolean existsByUserAndBadge_BadgeType(User user, String badgeType);
    
    long countByUser(User user);
}
