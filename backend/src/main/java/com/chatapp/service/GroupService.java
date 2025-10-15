package com.chatapp.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chatapp.model.Group;
import com.chatapp.model.GroupMember;
import com.chatapp.model.User;
import com.chatapp.repository.GroupMemberRepository;
import com.chatapp.repository.GroupRepository;
import com.chatapp.repository.UserRepository;

/**
 * Service for managing groups and group memberships.
 */
@Service
@Transactional
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;

    public GroupService(final GroupRepository groupRepository,
            final GroupMemberRepository groupMemberRepository,
            final UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
    }

    /**
     * Create a new group.
     *
     * @param name        group name
     * @param description group description
     * @param groupType   type of group
     * @param creatorId   ID of the creator
     * @param maxMembers  optional max member limit
     * @return created group
     */
    public Group createGroup(final String name, final String description,
            final Group.GroupType groupType, final Long creatorId,
            final Integer maxMembers) {
        final User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found"));

        final Group group = new Group(name, description, groupType, creator);
        group.setMaxMembers(maxMembers);

        final Group savedGroup = groupRepository.save(group);

        // Creator automatically becomes an ADMIN member
        final GroupMember creatorMembership = new GroupMember(savedGroup,
                creator, GroupMember.MemberRole.ADMIN);
        groupMemberRepository.save(creatorMembership);

        return savedGroup;
    }

    /**
     * Get all groups a user is a member of.
     *
     * @param userId user ID
     * @return list of groups
     */
    public List<Group> getUserGroups(final Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found"));

        return groupRepository.findGroupsByUserId(userId);
    }

    /**
     * Get all invite-only groups a user is a member of.
     *
     * @param userId user ID
     * @return list of invite-only groups
     */
    public List<Group> getUserInviteOnlyGroups(final Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found"));

        return groupRepository.findInviteOnlyGroupsByUserId(userId);
    }

    /**
     * Get group details by ID.
     *
     * @param groupId group ID
     * @return group details
     */
    public Group getGroupById(final Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Group not found"));
    }

    /**
     * Join a group using invite code.
     *
     * @param userId     user ID
     * @param inviteCode invite code
     * @return the group joined
     */
    public Group joinGroupByInviteCode(final Long userId,
            final String inviteCode) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found"));

        final Group group = groupRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid invite code"));

        if (group.getGroupType() != Group.GroupType.INVITE_ONLY) {
            throw new IllegalStateException(
                    "This group does not use invite codes");
        }

        // Check if already a member
        if (groupMemberRepository.existsByGroupAndUser(group, user)) {
            throw new IllegalStateException("Already a member of this group");
        }

        // Check max members limit
        if (group.getMaxMembers() != null) {
            final long currentMemberCount = groupMemberRepository
                    .countByGroup(group);
            if (currentMemberCount >= group.getMaxMembers()) {
                throw new IllegalStateException("Group is full");
            }
        }

        final GroupMember membership = new GroupMember(group, user,
                GroupMember.MemberRole.MEMBER);
        groupMemberRepository.save(membership);

        return group;
    }

    /**
     * Leave a group.
     *
     * @param userId  user ID
     * @param groupId group ID
     */
    public void leaveGroup(final Long userId, final Long groupId) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found"));

        final Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Group not found"));

        final GroupMember membership = groupMemberRepository
                .findByGroupAndUser(group, user)
                .orElseThrow(() -> new IllegalStateException(
                        "Not a member of this group"));

        // Check if user is the only admin
        final List<GroupMember> admins = groupMemberRepository
                .findByGroupAndRole(group, GroupMember.MemberRole.ADMIN);

        if (membership.getRole() == GroupMember.MemberRole.ADMIN
                && admins.size() == 1) {
            throw new IllegalStateException(
                    "Cannot leave: you are the only admin. Promote another member or delete the group.");
        }

        groupMemberRepository.delete(membership);
    }

    /**
     * Get all members of a group.
     *
     * @param groupId group ID
     * @return list of users
     */
    public List<User> getGroupMembers(final Long groupId) {
        final Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Group not found"));

        final List<GroupMember> members = groupMemberRepository
                .findByGroup(group);

        return members.stream().map(GroupMember::getUser)
                .collect(Collectors.toList());
    }

    /**
     * Get member count for a group.
     *
     * @param groupId group ID
     * @return member count
     */
    public long getGroupMemberCount(final Long groupId) {
        final Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Group not found"));

        return groupMemberRepository.countByGroup(group);
    }

    /**
     * Promote a member to admin.
     *
     * @param adminUserId   ID of user performing the action
     * @param groupId       group ID
     * @param targetUserId  ID of user to promote
     */
    public void promoteToAdmin(final Long adminUserId, final Long groupId,
            final Long targetUserId) {
        final User adminUser = userRepository.findById(adminUserId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found"));

        final User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Target user not found"));

        final Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Group not found"));

        // Check if admin user has admin role
        final GroupMember adminMembership = groupMemberRepository
                .findByGroupAndUser(group, adminUser)
                .orElseThrow(() -> new IllegalStateException(
                        "You are not a member of this group"));

        if (adminMembership.getRole() != GroupMember.MemberRole.ADMIN) {
            throw new IllegalStateException(
                    "Only admins can promote members");
        }

        // Promote target user
        final GroupMember targetMembership = groupMemberRepository
                .findByGroupAndUser(group, targetUser)
                .orElseThrow(() -> new IllegalStateException(
                        "Target user is not a member"));

        targetMembership.setRole(GroupMember.MemberRole.ADMIN);
        groupMemberRepository.save(targetMembership);
    }

    /**
     * Regenerate invite code for a group.
     *
     * @param userId  user ID
     * @param groupId group ID
     * @return new invite code
     */
    public String regenerateInviteCode(final Long userId, final Long groupId) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found"));

        final Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Group not found"));

        // Check if user is admin
        final GroupMember membership = groupMemberRepository
                .findByGroupAndUser(group, user)
                .orElseThrow(() -> new IllegalStateException(
                        "You are not a member of this group"));

        if (membership.getRole() != GroupMember.MemberRole.ADMIN) {
            throw new IllegalStateException(
                    "Only admins can regenerate invite codes");
        }

        if (group.getGroupType() != Group.GroupType.INVITE_ONLY) {
            throw new IllegalStateException(
                    "This group does not use invite codes");
        }

        group.regenerateInviteCode();
        groupRepository.save(group);

        return group.getInviteCode();
    }

    /**
     * Update group details.
     *
     * @param userId      user ID
     * @param groupId     group ID
     * @param name        new name
     * @param description new description
     * @return updated group
     */
    public Group updateGroup(final Long userId, final Long groupId,
            final String name, final String description) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found"));

        final Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Group not found"));

        // Check if user is admin
        final GroupMember membership = groupMemberRepository
                .findByGroupAndUser(group, user)
                .orElseThrow(() -> new IllegalStateException(
                        "You are not a member of this group"));

        if (membership.getRole() != GroupMember.MemberRole.ADMIN) {
            throw new IllegalStateException(
                    "Only admins can update group details");
        }

        // Update group details
        if (name != null && !name.trim().isEmpty()) {
            group.setName(name.trim());
        }
        if (description != null) {
            group.setDescription(description.trim());
        }

        return groupRepository.save(group);
    }

    /**
     * Add a member to a group.
     *
     * @param adminUserId user ID performing the action
     * @param groupId     group ID
     * @param username    username of user to add
     */
    public void addMember(final Long adminUserId, final Long groupId,
            final String username) {
        final User adminUser = userRepository.findById(adminUserId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found"));

        final User newUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found: " + username));

        final Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Group not found"));

        // Check if admin user has admin role
        final GroupMember adminMembership = groupMemberRepository
                .findByGroupAndUser(group, adminUser)
                .orElseThrow(() -> new IllegalStateException(
                        "You are not a member of this group"));

        if (adminMembership.getRole() != GroupMember.MemberRole.ADMIN) {
            throw new IllegalStateException(
                    "Only admins can add members");
        }

        // Check if already a member
        if (groupMemberRepository.existsByGroupAndUser(group, newUser)) {
            throw new IllegalStateException(
                    "User is already a member");
        }

        // Check max members limit
        if (group.getMaxMembers() != null) {
            final long currentMemberCount = groupMemberRepository
                    .countByGroup(group);
            if (currentMemberCount >= group.getMaxMembers()) {
                throw new IllegalStateException("Group is full");
            }
        }

        final GroupMember membership = new GroupMember(group, newUser,
                GroupMember.MemberRole.MEMBER);
        groupMemberRepository.save(membership);
    }

    /**
     * Remove a member from a group.
     *
     * @param adminUserId  user ID performing the action
     * @param groupId      group ID
     * @param targetUserId user ID to remove
     */
    public void removeMember(final Long adminUserId, final Long groupId,
            final Long targetUserId) {
        final User adminUser = userRepository.findById(adminUserId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found"));

        final User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Target user not found"));

        final Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Group not found"));

        // Check if admin user has admin role
        final GroupMember adminMembership = groupMemberRepository
                .findByGroupAndUser(group, adminUser)
                .orElseThrow(() -> new IllegalStateException(
                        "You are not a member of this group"));

        if (adminMembership.getRole() != GroupMember.MemberRole.ADMIN) {
            throw new IllegalStateException(
                    "Only admins can remove members");
        }

        // Find target membership
        final GroupMember targetMembership = groupMemberRepository
                .findByGroupAndUser(group, targetUser)
                .orElseThrow(() -> new IllegalStateException(
                        "Target user is not a member"));

        // Cannot remove another admin
        if (targetMembership.getRole() == GroupMember.MemberRole.ADMIN) {
            throw new IllegalStateException(
                    "Cannot remove an admin");
        }

        groupMemberRepository.delete(targetMembership);
    }

    /**
     * Check if a user can access a group (is a member).
     *
     * @param groupId group ID
     * @param userId  user ID
     * @return true if user is a member
     */
    public boolean canAccessGroup(final Long groupId, final Long userId) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found"));

        final Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Group not found"));

        return groupMemberRepository.existsByGroupAndUser(group, user);
    }
}
