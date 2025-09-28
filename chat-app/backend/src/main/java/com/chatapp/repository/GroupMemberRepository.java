package com.chatapp.repository;

import com.chatapp.model.GroupMember;
import com.chatapp.model.Group;
import com.chatapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    
    // ユーザーとグループでメンバーを検索
    Optional<GroupMember> findByUserAndGroupAndIsActive(User user, Group group, Boolean isActive);
    
    // グループのアクティブメンバー一覧
    List<GroupMember> findByGroupAndIsActive(Group group, Boolean isActive);
    
    // ユーザーが参加しているアクティブなグループ一覧
    List<GroupMember> findByUserAndIsActive(User user, Boolean isActive);
    
    // 匿名名で検索（同じグループ内）
    Optional<GroupMember> findByGroupAndAnonymousNameAndIsActive(Group group, String anonymousName, Boolean isActive);
    
    // グループ内のアクティブメンバー数をカウント
    @Query("SELECT COUNT(gm) FROM GroupMember gm WHERE gm.group = :group AND gm.isActive = true")
    Long countActiveMembers(@Param("group") Group group);
    
    // 匿名名の更新が必要なメンバーを取得（毎日0:00に実行）
    @Query("SELECT gm FROM GroupMember gm WHERE gm.lastAnonymousNameUpdate < :cutoffTime AND gm.isActive = true")
    List<GroupMember> findMembersNeedingAnonymousNameUpdate(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    // BANされていないアクティブメンバーを取得
    @Query("SELECT gm FROM GroupMember gm WHERE gm.group = :group AND gm.isActive = true " +
           "AND (gm.banUntil IS NULL OR gm.banUntil < :now)")
    List<GroupMember> findActiveNonBannedMembers(@Param("group") Group group, @Param("now") LocalDateTime now);
    
    // 警告回数が多いメンバーを取得
    @Query("SELECT gm FROM GroupMember gm WHERE gm.warningCount >= :warningThreshold AND gm.isActive = true")
    List<GroupMember> findMembersWithHighWarnings(@Param("warningThreshold") Integer warningThreshold);
}