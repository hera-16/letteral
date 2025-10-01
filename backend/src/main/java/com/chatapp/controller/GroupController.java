package com.chatapp.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chatapp.model.Group;
import com.chatapp.model.User;
import com.chatapp.security.UserPrincipal;
import com.chatapp.service.GroupService;

/**
 * REST controller for group management.
 */
@RestController
@RequestMapping("/api/groups")
@CrossOrigin(origins = "*")
public class GroupController {

    private final GroupService groupService;

    public GroupController(final GroupService groupService) {
        this.groupService = groupService;
    }

    /**
     * Create a new invite-only group.
     */
    @PostMapping("/invite")
    public ResponseEntity<Group> createInviteOnlyGroup(
            @RequestBody final CreateGroupRequest request,
            final Authentication authentication) {
        final Long userId = resolveUserId(authentication);
        final Group group = groupService.createGroup(
                request.getName(),
                request.getDescription(),
                Group.GroupType.INVITE_ONLY,
                userId,
                request.getMaxMembers());
        return ResponseEntity.status(HttpStatus.CREATED).body(group);
    }

    /**
     * Get all invite-only groups the user is a member of.
     */
    @GetMapping("/invite/my")
    public ResponseEntity<List<Group>> getMyInviteOnlyGroups(
            final Authentication authentication) {
        final Long userId = resolveUserId(authentication);
        final List<Group> groups = groupService.getUserInviteOnlyGroups(userId);
        return ResponseEntity.ok(groups);
    }

    /**
     * Join a group using invite code.
     */
    @PostMapping("/invite/join")
    public ResponseEntity<Group> joinByInviteCode(
            @RequestBody final JoinByCodeRequest request,
            final Authentication authentication) {
        final Long userId = resolveUserId(authentication);
        final Group group = groupService.joinGroupByInviteCode(userId,
                request.getInviteCode());
        return ResponseEntity.ok(group);
    }

    /**
     * Get group details by ID.
     */
    @GetMapping("/{groupId}")
    public ResponseEntity<Group> getGroupDetails(
            @PathVariable final Long groupId) {
        final Group group = groupService.getGroupById(groupId);
        return ResponseEntity.ok(group);
    }

    /**
     * Get members of a group.
     */
    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<User>> getGroupMembers(
            @PathVariable final Long groupId) {
        final List<User> members = groupService.getGroupMembers(groupId);
        return ResponseEntity.ok(members);
    }

    /**
     * Get member count for a group.
     */
    @GetMapping("/{groupId}/members/count")
    public ResponseEntity<Map<String, Long>> getGroupMemberCount(
            @PathVariable final Long groupId) {
        final long count = groupService.getGroupMemberCount(groupId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * Leave a group.
     */
    @DeleteMapping("/{groupId}/leave")
    public ResponseEntity<Void> leaveGroup(
            @PathVariable final Long groupId,
            final Authentication authentication) {
        final Long userId = resolveUserId(authentication);
        groupService.leaveGroup(userId, groupId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Promote a member to admin.
     */
    @PutMapping("/{groupId}/members/{memberId}/promote")
    public ResponseEntity<Void> promoteToAdmin(
            @PathVariable final Long groupId,
            @PathVariable final Long memberId,
            final Authentication authentication) {
        final Long userId = resolveUserId(authentication);
        groupService.promoteToAdmin(userId, groupId, memberId);
        return ResponseEntity.ok().build();
    }

    /**
     * Regenerate invite code.
     */
    @PutMapping("/{groupId}/invite-code/regenerate")
    public ResponseEntity<Map<String, String>> regenerateInviteCode(
            @PathVariable final Long groupId,
            final Authentication authentication) {
        final Long userId = resolveUserId(authentication);
        final String newCode = groupService.regenerateInviteCode(userId,
                groupId);
        return ResponseEntity.ok(Map.of("inviteCode", newCode));
    }

    /**
     * Update group details (name and description).
     */
    @PutMapping("/{groupId}")
    public ResponseEntity<Group> updateGroup(
            @PathVariable final Long groupId,
            @RequestBody final UpdateGroupRequest request,
            final Authentication authentication) {
        final Long userId = resolveUserId(authentication);
        final Group updatedGroup = groupService.updateGroup(userId, groupId,
                request.getName(), request.getDescription());
        return ResponseEntity.ok(updatedGroup);
    }

    /**
     * Add a member to a group.
     */
    @PostMapping("/{groupId}/members")
    public ResponseEntity<List<User>> addMember(
            @PathVariable final Long groupId,
            @RequestBody final AddMemberRequest request,
            final Authentication authentication) {
        final Long userId = resolveUserId(authentication);
        groupService.addMember(userId, groupId, request.getUsername());
        final List<User> members = groupService.getGroupMembers(groupId);
        return ResponseEntity.ok(members);
    }

    /**
     * Remove a member from a group.
     */
    @DeleteMapping("/{groupId}/members/{memberId}")
    public ResponseEntity<List<User>> removeMember(
            @PathVariable final Long groupId,
            @PathVariable final Long memberId,
            final Authentication authentication) {
        final Long userId = resolveUserId(authentication);
        groupService.removeMember(userId, groupId, memberId);
        final List<User> members = groupService.getGroupMembers(groupId);
        return ResponseEntity.ok(members);
    }

    /**
     * Get all public topics.
     */
    @GetMapping("/public")
    public ResponseEntity<List<Group>> getAllPublicTopics() {
        final List<Group> groups = groupService.getAllPublicTopics();
        return ResponseEntity.ok(groups);
    }

    /**
     * Join a public topic group.
     */
    @PostMapping("/public/{groupId}/join")
    public ResponseEntity<Group> joinPublicTopic(
            @PathVariable final Long groupId,
            final Authentication authentication) {
        final Long userId = resolveUserId(authentication);
        final Group group = groupService.joinPublicTopic(userId, groupId);
        return ResponseEntity.ok(group);
    }

    private Long resolveUserId(final Authentication authentication) {
        final Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getId();
        }
        throw new IllegalStateException("Unsupported principal: " + principal);
    }

    // DTOs

    public static final class CreateGroupRequest {
        private String name;
        private String description;
        private Integer maxMembers;

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(final String description) {
            this.description = description;
        }

        public Integer getMaxMembers() {
            return maxMembers;
        }

        public void setMaxMembers(final Integer maxMembers) {
            this.maxMembers = maxMembers;
        }
    }

    public static final class JoinByCodeRequest {
        private String inviteCode;

        public String getInviteCode() {
            return inviteCode;
        }

        public void setInviteCode(final String inviteCode) {
            this.inviteCode = inviteCode;
        }
    }

    public static final class UpdateGroupRequest {
        private String name;
        private String description;

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(final String description) {
            this.description = description;
        }
    }

    public static final class AddMemberRequest {
        private String username;

        public String getUsername() {
            return username;
        }

        public void setUsername(final String username) {
            this.username = username;
        }
    }
}
