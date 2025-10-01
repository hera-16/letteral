package com.chatapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.chatapp.model.Group;
import com.chatapp.model.GroupMember;
import com.chatapp.model.User;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    /**
     * Find a specific membership by group and user.
     *
     * @param group the group
     * @param user  the user
     * @return the membership if found
     */
    Optional<GroupMember> findByGroupAndUser(Group group, User user);

    /**
     * Find all members of a specific group.
     *
     * @param group the group
     * @return list of group members
     */
    List<GroupMember> findByGroup(Group group);

    /**
     * Find all groups a user is a member of.
     *
     * @param user the user
     * @return list of group memberships
     */
    List<GroupMember> findByUser(User user);

    /**
     * Count the number of members in a group.
     *
     * @param group the group
     * @return member count
     */
    long countByGroup(Group group);

    /**
     * Check if a user is a member of a group.
     *
     * @param group the group
     * @param user  the user
     * @return true if the user is a member
     */
    boolean existsByGroupAndUser(Group group, User user);

    /**
     * Find all admins of a specific group.
     *
     * @param group the group
     * @param role  the role
     * @return list of admin members
     */
    @Query("SELECT gm FROM GroupMember gm WHERE gm.group = :group AND gm.role = :role")
    List<GroupMember> findByGroupAndRole(@Param("group") Group group,
            @Param("role") GroupMember.MemberRole role);

    /**
     * Delete a specific membership.
     *
     * @param group the group
     * @param user  the user
     */
    void deleteByGroupAndUser(Group group, User user);
}
