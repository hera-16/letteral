package com.chatapp.repository;

import com.chatapp.model.Group;
import com.chatapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    
    // 招待コードでグループを検索
    Optional<Group> findByInviteCode(String inviteCode);
    
    // グループタイプで検索
    List<Group> findByGroupType(Group.GroupType groupType);
    
    // 一般グループ（トピック別）を取得
    @Query("SELECT g FROM Group g WHERE g.groupType = 'PUBLIC_TOPIC' ORDER BY g.createdAt DESC")
    List<Group> findPublicTopicGroups();
    
    // 招待制グループを取得
    @Query("SELECT g FROM Group g WHERE g.groupType = 'INVITE_ONLY' ORDER BY g.createdAt DESC")
    List<Group> findInviteOnlyGroups();
    
    // 名前で検索（部分一致）
    @Query("SELECT g FROM Group g WHERE g.name LIKE %:name% AND g.groupType = 'PUBLIC_TOPIC'")
    List<Group> findByNameContaining(@Param("name") String name);
    
    // アクティブなメンバー数を取得
    @Query("SELECT COUNT(gm) FROM GroupMember gm WHERE gm.group.id = :groupId AND gm.isActive = true")
    Long countActiveMembers(@Param("groupId") Long groupId);
    
    // 満員でないグループを取得
    @Query("SELECT g FROM Group g WHERE " +
           "(SELECT COUNT(gm) FROM GroupMember gm WHERE gm.group.id = g.id AND gm.isActive = true) < g.maxMembers")
    List<Group> findAvailableGroups();
    
    // ユーザーが参加しているグループを取得
    @Query("SELECT g FROM Group g JOIN GroupMember gm ON g.id = gm.group.id " +
           "WHERE gm.user = :user AND gm.isActive = true")
    List<Group> findByUser(@Param("user") User user);
    
    // 公開グループを取得
    @Query("SELECT g FROM Group g WHERE g.groupType = 'PUBLIC_TOPIC' ORDER BY g.createdAt DESC")
    List<Group> findPublicGroups();
    
    // キーワードで公開グループを検索
    @Query("SELECT g FROM Group g WHERE g.groupType = 'PUBLIC_TOPIC' " +
           "AND (g.name LIKE %:keyword% OR g.description LIKE %:keyword%) " +
           "ORDER BY g.createdAt DESC")
    List<Group> findPublicGroupsByKeyword(@Param("keyword") String keyword);
}