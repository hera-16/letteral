package com.chatapp.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    private Long resolveUserId(final Authentication authentication) {
        final Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getId();
        }
        throw new IllegalStateException("Unsupported principal: " + principal);
    }

    /**
     * DTO representing friend statistics.
     *
     * @param friendCount         total accepted friends
     * @param pendingRequestCount total pending requests
     */
    public record FriendStatsResponse(long friendCount,
            long pendingRequestCount) {
    }
}
