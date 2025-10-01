package com.chatapp.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chatapp.model.Friend;
import com.chatapp.model.User;
import com.chatapp.security.UserPrincipal;
import com.chatapp.service.FriendService;

/**
 * Provides friend-related REST endpoints.
 */
@RestController
@RequestMapping("/api/friends")
@CrossOrigin(origins = "*")
public final class FriendController {

    private final FriendService friendService;

    public FriendController(final FriendService friendService) {
        this.friendService = friendService;
    }

    /**
     * Returns aggregated statistics for the authenticated user.
     *
     * @param authentication current security context principal
     * @return friend and pending request counts
     */
    @GetMapping("/stats")
    public ResponseEntity<FriendStatsResponse> getFriendStats(
            final Authentication authentication) {
        final Long userId = resolveUserId(authentication);
        final List<User> friends = friendService.getFriends(userId);
        final long friendCount = friends.size();
        final long pendingCount = Optional
                .ofNullable(friendService.getPendingRequestCount(userId))
                .orElse(0L);
        final FriendStatsResponse response = new FriendStatsResponse(friendCount,
                pendingCount);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all friends of the authenticated user.
     */
    @GetMapping("/list")
    public ResponseEntity<List<User>> getFriends(
            final Authentication authentication) {
        final Long userId = resolveUserId(authentication);
        final List<User> friends = friendService.getFriends(userId);
        return ResponseEntity.ok(friends);
    }

    /**
     * Get all friends with friendship IDs.
     */
    @GetMapping("/list/detailed")
    public ResponseEntity<List<FriendWithIdDto>> getFriendsWithIds(
            final Authentication authentication) {
        System.out.println("getFriendsWithIds called, authentication: " + authentication);
        System.out.println("Principal: " + (authentication != null ? authentication.getPrincipal() : "null"));
        final Long userId = resolveUserId(authentication);
        System.out.println("Resolved userId: " + userId);
        final List<FriendWithIdDto> friends = friendService.getFriendsWithIds(userId);
        System.out.println("Found " + friends.size() + " friends");
        return ResponseEntity.ok(friends);
    }

    /**
     * Get all pending friend requests (received).
     */
    @GetMapping("/requests/pending")
    public ResponseEntity<List<Friend>> getPendingRequests(
            final Authentication authentication) {
        final Long userId = resolveUserId(authentication);
        final List<Friend> requests = friendService.getPendingRequests(userId);
        return ResponseEntity.ok(requests);
    }

    /**
     * Get all sent friend requests.
     */
    @GetMapping("/requests/sent")
    public ResponseEntity<List<Friend>> getSentRequests(
            final Authentication authentication) {
        final Long userId = resolveUserId(authentication);
        final List<Friend> requests = friendService.getSentRequests(userId);
        return ResponseEntity.ok(requests);
    }

    /**
     * Send a friend request by username.
     */
    @PostMapping("/request/{username}")
    public ResponseEntity<Friend> sendFriendRequest(
            @PathVariable final String username,
            final Authentication authentication) {
        final Long userId = resolveUserId(authentication);
        final Friend request = friendService.sendFriendRequest(userId, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(request);
    }

    /**
     * Accept a friend request.
     */
    @PostMapping("/accept/{requestId}")
    public ResponseEntity<Friend> acceptFriendRequest(
            @PathVariable final Long requestId,
            final Authentication authentication) {
        final Long userId = resolveUserId(authentication);
        final Friend friend = friendService.acceptFriendRequest(userId,
                requestId);
        return ResponseEntity.ok(friend);
    }

    /**
     * Reject a friend request.
     */
    @PostMapping("/reject/{requestId}")
    public ResponseEntity<Friend> rejectFriendRequest(
            @PathVariable final Long requestId,
            final Authentication authentication) {
        final Long userId = resolveUserId(authentication);
        final Friend friend = friendService.rejectFriendRequest(userId,
                requestId);
        return ResponseEntity.ok(friend);
    }

    /**
     * Remove a friend.
     */
    @DeleteMapping("/{friendId}")
    public ResponseEntity<Void> removeFriend(
            @PathVariable final Long friendId,
            final Authentication authentication) {
        final Long userId = resolveUserId(authentication);
        friendService.removeFriend(userId, friendId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Block a user.
     */
    @PostMapping("/block/{targetUserId}")
    public ResponseEntity<Friend> blockUser(
            @PathVariable final Long targetUserId,
            final Authentication authentication) {
        final Long userId = resolveUserId(authentication);
        final Friend blocked = friendService.blockUser(userId, targetUserId);
        return ResponseEntity.ok(blocked);
    }

    /**
     * Reset all friendships for the authenticated user (admin/testing purpose).
     */
    @DeleteMapping("/reset")
    public ResponseEntity<String> resetAllFriendships(
            final Authentication authentication) {
        final Long userId = resolveUserId(authentication);
        friendService.resetAllFriendships(userId);
        return ResponseEntity.ok("All friendships have been reset");
    }

    /**
     * Get all friendships with details for the authenticated user (admin/testing purpose).
     */
    @GetMapping("/debug/all")
    public ResponseEntity<List<FriendshipDetail>> getAllFriendshipsWithDetails(
            final Authentication authentication) {
        final Long userId = resolveUserId(authentication);
        final List<FriendshipDetail> details = friendService.getAllFriendshipsWithDetails(userId);
        return ResponseEntity.ok(details);
    }

    private Long resolveUserId(final Authentication authentication) {
        final Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getId();
        }
        throw new IllegalStateException("Unsupported principal: " + principal);
    }

    /**
     * DTO representing detailed friendship information.
     */
    public static final class FriendshipDetail {
        private final Long friendshipId;
        private final String requesterUsername;
        private final String requesterDisplayName;
        private final String addresseeUsername;
        private final String addresseeDisplayName;
        private final String status;
        private final String createdAt;

        public FriendshipDetail(final Long friendshipId,
                final String requesterUsername, final String requesterDisplayName,
                final String addresseeUsername, final String addresseeDisplayName,
                final String status, final String createdAt) {
            this.friendshipId = friendshipId;
            this.requesterUsername = requesterUsername;
            this.requesterDisplayName = requesterDisplayName;
            this.addresseeUsername = addresseeUsername;
            this.addresseeDisplayName = addresseeDisplayName;
            this.status = status;
            this.createdAt = createdAt;
        }

        public Long getFriendshipId() { return friendshipId; }
        public String getRequesterUsername() { return requesterUsername; }
        public String getRequesterDisplayName() { return requesterDisplayName; }
        public String getAddresseeUsername() { return addresseeUsername; }
        public String getAddresseeDisplayName() { return addresseeDisplayName; }
        public String getStatus() { return status; }
        public String getCreatedAt() { return createdAt; }
    }

    /**
     * DTO representing friend with ID.
     */
    public static final class FriendWithIdDto {
        private final Long friendshipId;
        private final Long userId;
        private final String username;
        private final String displayName;
        private final String email;

        public FriendWithIdDto(final Long friendshipId, final Long userId,
                final String username, final String displayName, final String email) {
            this.friendshipId = friendshipId;
            this.userId = userId;
            this.username = username;
            this.displayName = displayName;
            this.email = email;
        }

        public Long getFriendshipId() { return friendshipId; }
        public Long getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getDisplayName() { return displayName; }
        public String getEmail() { return email; }
    }

    /**
     * DTO representing friend statistics.
     *
     * @param friendCount         total accepted friends
     * @param pendingRequestCount total pending requests
     */
    public static final class FriendStatsResponse {

        private final long friendCount;
        private final long pendingRequestCount;

        public FriendStatsResponse(final long friendCount,
                final long pendingRequestCount) {
            this.friendCount = friendCount;
            this.pendingRequestCount = pendingRequestCount;
        }

        public long getFriendCount() {
            return friendCount;
        }

        public long getPendingRequestCount() {
            return pendingRequestCount;
        }
    }
}
