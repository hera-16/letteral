package com.chatapp.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chatapp.model.User;
import com.chatapp.repository.UserRepository;
import com.chatapp.security.UserPrincipal;

/**
 * Provides user-related REST endpoints.
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserRepository userRepository;

    public UserController(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Get current authenticated user info.
     */
    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(final Authentication authentication) {
        final Long userId = resolveUserId(authentication);
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return ResponseEntity.ok(toDto(user));
    }

    /**
     * Search users by username (for friend requests).
     * Returns users whose username contains the search query.
     */
    @GetMapping("/search")
    public ResponseEntity<List<UserDto>> searchUsers(
            @RequestParam final String query,
            final Authentication authentication) {
        final Long currentUserId = resolveUserId(authentication);
        
        // Search users by username containing the query (case-insensitive)
        final List<User> users = userRepository.findAll().stream()
                .filter(user -> !user.getId().equals(currentUserId)) // Exclude current user
                .filter(user -> user.getUsername().toLowerCase().contains(query.toLowerCase()))
                .limit(20) // Limit results
                .collect(Collectors.toList());
        
        final List<UserDto> userDtos = users.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(userDtos);
    }

    /**
     * Get user by username.
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<UserDto> getUserByUsername(@PathVariable final String username) {
        final User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        return ResponseEntity.ok(toDto(user));
    }

    /**
     * Get all users (for development/testing - consider removing in production).
     */
    @GetMapping("/all")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        final List<UserDto> users = userRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    private Long resolveUserId(final Authentication authentication) {
        final Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getId();
        }
        throw new IllegalStateException("Unsupported principal: " + principal);
    }

    private UserDto toDto(final User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getEmail()
        );
    }

    /**
     * DTO for user information (without sensitive data).
     */
    public static class UserDto {
        private final Long id;
        private final String username;
        private final String displayName;
        private final String email;

        public UserDto(final Long id, final String username, 
                       final String displayName, final String email) {
            this.id = id;
            this.username = username;
            this.displayName = displayName;
            this.email = email;
        }

        public Long getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getEmail() {
            return email;
        }
    }
}
