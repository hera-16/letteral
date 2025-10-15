package com.chatapp.repository;

import com.chatapp.model.Badge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * バッジリポジトリ
 */
@Repository
public interface BadgeRepository extends JpaRepository<Badge, Long> {
    
    Optional<Badge> findByBadgeType(String badgeType);
}
