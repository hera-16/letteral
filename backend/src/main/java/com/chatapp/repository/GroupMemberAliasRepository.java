package com.chatapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.chatapp.model.Group;
import com.chatapp.model.GroupMemberAlias;
import com.chatapp.model.User;

@Repository
public interface GroupMemberAliasRepository extends JpaRepository<GroupMemberAlias, Long> {
    
    /**
     * グループ内で特定のユーザーの匿名名を取得
     * 全員が同じ匿名名を見る
     */
    Optional<GroupMemberAlias> findByTargetUserAndGroup(User targetUser, Group group);
    
    /**
     * 特定のグループ内のすべての匿名名マッピングを取得
     */
    List<GroupMemberAlias> findByGroup(Group group);
    
    /**
     * グループIDで匿名名マッピングを取得
     */
    @Query("SELECT a FROM GroupMemberAlias a WHERE a.group.id = :groupId")
    List<GroupMemberAlias> findByGroupId(@Param("groupId") Long groupId);
}
