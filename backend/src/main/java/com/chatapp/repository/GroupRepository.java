package com.chatapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.chatapp.model.Group;
import com.chatapp.model.User;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    /**
     * Find a group by its invite code.
     *
     * @param inviteCode the invite code
     * @return the group if found
     */
    Optional<Group> findByInviteCode(String inviteCode);

    /**
     * Find all groups created by a specific user.
     *
     * @param creator the user who created the groups
     * @return list of groups
     */
    List<Group> findByCreator(User creator);

    /**
     * Find all groups of a specific type.
     *
     * @param groupType the type of group
     * @return list of groups
     */
    List<Group> findByGroupType(Group.GroupType groupType);

    /**
     * Find all groups that a user is a member of.
     *
     * @param userId the user ID
     * @return list of groups
     */
    @Query("SELECT g FROM Group g JOIN g.members m WHERE m.user.id = :userId ORDER BY g.createdAt DESC")
    List<Group> findGroupsByUserId(@Param("userId") Long userId);

    /**
     * Find invite-only groups that a user is a member of.
     *
     * @param userId the user ID
     * @return list of invite-only groups
     */
    @Query("SELECT g FROM Group g JOIN g.members m WHERE m.user.id = :userId AND g.groupType = 'INVITE_ONLY' ORDER BY g.createdAt DESC")
    List<Group> findInviteOnlyGroupsByUserId(@Param("userId") Long userId);
}
