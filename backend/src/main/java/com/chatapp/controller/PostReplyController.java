package com.chatapp.controller;

import com.chatapp.dto.CreatePostReplyRequest;
import com.chatapp.dto.PostReplyDTO;
import com.chatapp.model.User;
import com.chatapp.repository.UserRepository;
import com.chatapp.service.PostReplyService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 投稿返信コントローラー
 */
@RestController
@RequestMapping("/api/replies")
public class PostReplyController {

    private final PostReplyService postReplyService;
    private final UserRepository userRepository;

    public PostReplyController(PostReplyService postReplyService, UserRepository userRepository) {
        this.postReplyService = postReplyService;
        this.userRepository = userRepository;
    }

    /**
     * 返信を作成
     * POST /api/replies
     */
    @PostMapping
    public ResponseEntity<PostReplyDTO> createReply(
            @Valid @RequestBody CreatePostReplyRequest request,
            Authentication authentication) {

        System.out.println("DEBUG: Received reply request - postId: " + request.getPostId() + ", content length: " +
            (request.getContent() != null ? request.getContent().length() : "null"));

        // ユーザー名からユーザーを取得
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));

        PostReplyDTO reply = postReplyService.createReply(user.getId(), request);

        return ResponseEntity.status(HttpStatus.CREATED).body(reply);
    }

    /**
     * 特定の投稿に対する返信一覧を取得
     * GET /api/replies/post/{postId}
     */
    @GetMapping("/post/{postId}")
    public ResponseEntity<List<PostReplyDTO>> getRepliesByPostId(
            @PathVariable Long postId,
            Authentication authentication) {

        // ユーザー名からユーザーを取得
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));

        List<PostReplyDTO> replies = postReplyService.getRepliesByPostId(postId, user.getId());

        return ResponseEntity.ok(replies);
    }

    /**
     * 投稿の返信数を取得
     * GET /api/replies/post/{postId}/count
     */
    @GetMapping("/post/{postId}/count")
    public ResponseEntity<Long> getReplyCount(@PathVariable Long postId) {
        long count = postReplyService.getReplyCount(postId);
        return ResponseEntity.ok(count);
    }
}
