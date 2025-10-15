package com.chatapp.controller;

import com.chatapp.dto.ApiResponse;
import com.chatapp.dto.ChallengeShareDtos.ChallengeShareResponse;
import com.chatapp.dto.ChallengeShareDtos.CreateShareRequest;
import com.chatapp.dto.ChallengeShareDtos.PagedShareResponse;
import com.chatapp.dto.ChallengeShareDtos.ReactionRequest;
import com.chatapp.model.ChallengeShareReaction.ReactionType;
import com.chatapp.model.User;
import com.chatapp.repository.UserRepository;
import com.chatapp.service.ChallengeShareService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/shares")
public class ChallengeShareController {

    private final ChallengeShareService shareService;
    private final UserRepository userRepository;

    public ChallengeShareController(ChallengeShareService shareService, UserRepository userRepository) {
        this.shareService = shareService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ChallengeShareResponse>> createShare(
            @RequestBody CreateShareRequest request,
            Authentication authentication) {
        try {
            User user = resolveUser(authentication);
            if (request.getChallengeId() == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("challengeIdは必須です"));
            }
            ChallengeShareResponse response = shareService.createShare(
                    user.getId(),
                    request.getChallengeId(),
                    request.getComment(),
                    request.getMood());
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("共有の作成に失敗しました: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedShareResponse>> getTimeline(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        try {
            User user = resolveUser(authentication);
            PagedShareResponse response = shareService.getTimeline(user.getId(), page, size);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("タイムラインの取得に失敗しました: " + e.getMessage()));
        }
    }

    @PostMapping("/{shareId}/reactions")
    public ResponseEntity<ApiResponse<ChallengeShareResponse>> addReaction(
            @PathVariable Long shareId,
            @RequestBody ReactionRequest request,
            Authentication authentication) {
        try {
            User user = resolveUser(authentication);
            ReactionType type = parseReactionType(request);
            ChallengeShareResponse response = shareService.addOrUpdateReaction(user.getId(), shareId, type);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("リアクションの処理に失敗しました: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{shareId}/reactions/{type}")
    public ResponseEntity<ApiResponse<ChallengeShareResponse>> removeReaction(
            @PathVariable Long shareId,
            @PathVariable String type,
            Authentication authentication) {
        try {
            User user = resolveUser(authentication);
            ReactionType reactionType = parseReactionType(type);
            ChallengeShareResponse response = shareService.removeReaction(user.getId(), shareId, reactionType);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("リアクションの削除に失敗しました: " + e.getMessage()));
        }
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(Authentication authentication) {
        try {
            User user = resolveUser(authentication);
            long count = shareService.getUnreadCount(user.getId());
            return ResponseEntity.ok(ApiResponse.success(Map.of("count", count)));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("未読数の取得に失敗しました: " + e.getMessage()));
        }
    }

    @PostMapping("/mark-read")
    public ResponseEntity<ApiResponse<Void>> markRead(Authentication authentication) {
        try {
            User user = resolveUser(authentication);
            shareService.markTimelineRead(user.getId());
            return ResponseEntity.ok(ApiResponse.success(null));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("既読処理に失敗しました: " + e.getMessage()));
        }
    }

    private User resolveUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("ユーザーが見つかりません"));
    }

    private ReactionType parseReactionType(ReactionRequest request) {
        if (request == null || request.getType() == null) {
            throw new IllegalArgumentException("typeは必須です");
        }
        return parseReactionType(request.getType());
    }

    private ReactionType parseReactionType(String type) {
        try {
            return ReactionType.valueOf(type.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("無効なリアクションタイプです: " + type);
        }
    }
}
