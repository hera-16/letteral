package com.chatapp.controller;

import com.chatapp.model.PostReaction;
import com.chatapp.model.User;
import com.chatapp.model.ProgressPost;
import com.chatapp.model.enums.ReactionType;
import com.chatapp.service.PostReactionService;
import com.chatapp.service.ProgressPostService;
import com.chatapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * リアクションのREST APIエンドポイント
 */
@RestController
@RequestMapping("/api/reactions")
public class PostReactionController {

    private static final Logger log = LoggerFactory.getLogger(PostReactionController.class);

    private final PostReactionService postReactionService;
    private final ProgressPostService progressPostService;
    private final UserRepository userRepository;

    public PostReactionController(
            PostReactionService postReactionService,
            ProgressPostService progressPostService,
            UserRepository userRepository) {
        this.postReactionService = postReactionService;
        this.progressPostService = progressPostService;
        this.userRepository = userRepository;
    }

    /**
     * リアクションを追加
     */
    @PostMapping
    public ResponseEntity<PostReaction> addReaction(@RequestBody PostReaction reaction) {
        log.info("Adding reaction to post: {}", reaction.getPost().getId());
        PostReaction created = postReactionService.addReaction(reaction);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * リアクションを削除
     */
    @DeleteMapping("/{reactionId}")
    public ResponseEntity<Void> deleteReaction(@PathVariable Long reactionId) {
        log.info("Deleting reaction: {}", reactionId);
        postReactionService.deleteReaction(reactionId);
        return ResponseEntity.noContent().build();
    }

    /**
     * リアクションIDで取得
     */
    @GetMapping("/{reactionId}")
    public ResponseEntity<PostReaction> getReaction(@PathVariable Long reactionId) {
        log.info("Fetching reaction: {}", reactionId);
        PostReaction reaction = postReactionService.getReactionById(reactionId);
        return ResponseEntity.ok(reaction);
    }

    /**
     * 投稿のすべてのリアクション取得
     */
    @GetMapping("/post/{postId}")
    public ResponseEntity<List<PostReaction>> getReactionsByPost(@PathVariable Long postId) {
        log.info("Fetching reactions for post: {}", postId);
        ProgressPost post = progressPostService.getPostById(postId);
        List<PostReaction> reactions = postReactionService.getReactionsByPost(post);
        return ResponseEntity.ok(reactions);
    }

    /**
     * 投稿×リアクションタイプで取得
     */
    @GetMapping("/post/{postId}/type/{reactionType}")
    public ResponseEntity<List<PostReaction>> getReactionsByPostAndType(
            @PathVariable Long postId,
            @PathVariable ReactionType reactionType) {
        log.info("Fetching {} reactions for post: {}", reactionType, postId);
        ProgressPost post = progressPostService.getPostById(postId);
        List<PostReaction> reactions = postReactionService.getReactionsByPostAndType(post, reactionType);
        return ResponseEntity.ok(reactions);
    }

    /**
     * ユーザーの特定投稿へのリアクション取得
     */
    @GetMapping("/post/{postId}/user/{userId}/type/{reactionType}")
    public ResponseEntity<PostReaction> getUserReaction(
            @PathVariable Long postId,
            @PathVariable Long userId,
            @PathVariable ReactionType reactionType) {
        log.info("Fetching reaction by user {} on post {} with type {}", userId, postId, reactionType);
        ProgressPost post = progressPostService.getPostById(postId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
        Optional<PostReaction> reaction = postReactionService.getUserReaction(post, user, reactionType);
        return reaction.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 複数投稿のリアクション一括取得
     */
    @PostMapping("/posts/batch")
    public ResponseEntity<List<PostReaction>> getReactionsByPosts(@RequestBody List<Long> postIds) {
        log.info("Fetching reactions for {} posts", postIds.size());
        List<ProgressPost> posts = postIds.stream()
                .map(progressPostService::getPostById)
                .toList();
        List<PostReaction> reactions = postReactionService.getReactionsByPosts(posts);
        return ResponseEntity.ok(reactions);
    }

    /**
     * 投稿のリアクション総数取得
     */
    @GetMapping("/post/{postId}/count")
    public ResponseEntity<Long> countReactionsByPost(@PathVariable Long postId) {
        log.info("Counting reactions for post: {}", postId);
        ProgressPost post = progressPostService.getPostById(postId);
        long count = postReactionService.countReactionsByPost(post);
        return ResponseEntity.ok(count);
    }

    /**
     * 投稿×リアクションタイプの数をカウント
     */
    @GetMapping("/post/{postId}/type/{reactionType}/count")
    public ResponseEntity<Long> countReactionsByPostAndType(
            @PathVariable Long postId,
            @PathVariable ReactionType reactionType) {
        log.info("Counting {} reactions for post: {}", reactionType, postId);
        ProgressPost post = progressPostService.getPostById(postId);
        long count = postReactionService.countReactionsByPostAndType(post, reactionType);
        return ResponseEntity.ok(count);
    }

    /**
     * 投稿のリアクションタイプ別集計
     */
    @GetMapping("/post/{postId}/summary")
    public ResponseEntity<Map<ReactionType, Long>> getReactionSummary(@PathVariable Long postId) {
        log.info("Fetching reaction summary for post: {}", postId);
        ProgressPost post = progressPostService.getPostById(postId);
        Map<ReactionType, Long> summary = postReactionService.getReactionSummary(post);
        return ResponseEntity.ok(summary);
    }

    /**
     * ユーザーのリアクション削除
     */
    @DeleteMapping("/post/{postId}/user/{userId}/type/{reactionType}")
    public ResponseEntity<Void> deleteUserReaction(
            @PathVariable Long postId,
            @PathVariable Long userId,
            @PathVariable ReactionType reactionType) {
        log.info("Deleting reaction from user {} on post {} with type {}", userId, postId, reactionType);
        ProgressPost post = progressPostService.getPostById(postId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
        postReactionService.deleteUserReaction(post, user, reactionType);
        return ResponseEntity.noContent().build();
    }

    /**
     * ユーザーが既にリアクションしているか確認
     */
    @GetMapping("/post/{postId}/user/{userId}/type/{reactionType}/exists")
    public ResponseEntity<Boolean> hasUserReacted(
            @PathVariable Long postId,
            @PathVariable Long userId,
            @PathVariable ReactionType reactionType) {
        log.info("Checking if user {} has reacted to post {} with type {}", userId, postId, reactionType);
        ProgressPost post = progressPostService.getPostById(postId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
        boolean hasReacted = postReactionService.hasUserReacted(post, user, reactionType);
        return ResponseEntity.ok(hasReacted);
    }

    /**
     * リアクションのトグル（追加/削除）
     */
    @PostMapping("/post/{postId}/user/{userId}/type/{reactionType}/toggle")
    public ResponseEntity<PostReaction> toggleReaction(
            @PathVariable Long postId,
            @PathVariable Long userId,
            @PathVariable ReactionType reactionType) {
        log.info("Toggling reaction {} from user {} on post {}", reactionType, userId, postId);
        ProgressPost post = progressPostService.getPostById(postId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
        PostReaction result = postReactionService.toggleReaction(post, user, reactionType);

        if (result == null) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(result);
        }
    }
}

