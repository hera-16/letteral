package com.chatapp.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chatapp.dto.FriendRequestDto;
import com.chatapp.model.Friend;
import com.chatapp.model.User;
import com.chatapp.repository.UserRepository;
import com.chatapp.service.FriendService;

@RestController
@RequestMapping("/api/friends")
@CrossOrigin(origins = "http://localhost:3000")
public class FriendController {

    @Autowired
    private FriendService friendService;

    @Autowired
    private UserRepository userRepository;

    /**
     * フレンド申請を送信
     */
    @PostMapping("/request")
    public ResponseEntity<Friend> sendFriendRequest(
            @RequestBody FriendRequestDto request,
            Authentication authentication) {
        
        Long userId = getUserIdFromUsername(authentication.getName());
        Friend friendRequest = friendService.sendFriendRequest(userId, request.getTargetUserId());
        return ResponseEntity.ok(friendRequest);
    }

    /**
     * フレンド申請を承認
     */
    @PostMapping("/accept/{requestId}")
    public ResponseEntity<Friend> acceptFriendRequest(
            @PathVariable Long requestId,
            Authentication authentication) {
        
        Long userId = getUserIdFromUsername(authentication.getName());
        Friend acceptedRequest = friendService.acceptFriendRequest(userId, requestId);
        return ResponseEntity.ok(acceptedRequest);
    }

    /**
     * フレンド申請を拒否
     */
    @PostMapping("/reject/{requestId}")
    public ResponseEntity<Friend> rejectFriendRequest(
            @PathVariable Long requestId,
            Authentication authentication) {
        
        Long userId = getUserIdFromUsername(authentication.getName());
        Friend rejectedRequest = friendService.rejectFriendRequest(userId, requestId);
        return ResponseEntity.ok(rejectedRequest);
    }

    /**
     * ユーザーをブロック
     */
    @PostMapping("/block/{targetUserId}")
    public ResponseEntity<Friend> blockUser(
            @PathVariable Long targetUserId,
            Authentication authentication) {
        
        Long userId = getUserIdFromUsername(authentication.getName());
        Friend blockRelation = friendService.blockUser(userId, targetUserId);
        return ResponseEntity.ok(blockRelation);
    }

    /**
     * フレンドを削除
     */
    @DeleteMapping("/{friendId}")
    public ResponseEntity<Void> removeFriend(
            @PathVariable Long friendId,
            Authentication authentication) {
        
        Long userId = getUserIdFromUsername(authentication.getName());
        friendService.removeFriend(userId, friendId);
        return ResponseEntity.ok().build();
    }

    /**
     * フレンド一覧を取得
     */
    @GetMapping("/list")
    public ResponseEntity<List<User>> getFriends(Authentication authentication) {
        Long userId = getUserIdFromUsername(authentication.getName());
        List<User> friends = friendService.getFriends(userId);
        return ResponseEntity.ok(friends);
    }

    /**
     * 受信したフレンド申請一覧を取得
     */
    @GetMapping("/requests/received")
    public ResponseEntity<List<Friend>> getPendingRequests(Authentication authentication) {
        Long userId = getUserIdFromUsername(authentication.getName());
        List<Friend> requests = friendService.getPendingRequests(userId);
        return ResponseEntity.ok(requests);
    }

    /**
     * 送信したフレンド申請一覧を取得
     */
    @GetMapping("/requests/sent")
    public ResponseEntity<List<Friend>> getSentRequests(Authentication authentication) {
        Long userId = getUserIdFromUsername(authentication.getName());
        List<Friend> requests = friendService.getSentRequests(userId);
        return ResponseEntity.ok(requests);
    }

    /**
     * フレンド申請の統計情報を取得
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getFriendStats(Authentication authentication) {
        Long userId = getUserIdFromUsername(authentication.getName());
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("friendCount", friendService.getFriends(userId).size());
        stats.put("pendingRequestCount", friendService.getPendingRequestCount(userId));
        
        return ResponseEntity.ok(stats);
    }

    private Long getUserIdFromUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        return user.getId();
    }
}