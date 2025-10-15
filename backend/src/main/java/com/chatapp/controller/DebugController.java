package com.chatapp.controller;

import com.chatapp.dto.ApiResponse;
import com.chatapp.repository.ChallengeCompletionRepository;
import com.chatapp.repository.UserBadgeRepository;
import com.chatapp.repository.UserProgressRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * デバッグ用コントローラー（開発環境のみ）
 */
@RestController
@RequestMapping("/api/debug")
public class DebugController {
    
    private final ChallengeCompletionRepository challengeCompletionRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final UserProgressRepository userProgressRepository;
    
    public DebugController(
            ChallengeCompletionRepository challengeCompletionRepository,
            UserBadgeRepository userBadgeRepository,
            UserProgressRepository userProgressRepository) {
        this.challengeCompletionRepository = challengeCompletionRepository;
        this.userBadgeRepository = userBadgeRepository;
        this.userProgressRepository = userProgressRepository;
    }
    
    /**
     * 全てのチャレンジ達成記録をリセット（開発用）
     * DELETE /api/debug/reset-all
     */
    @DeleteMapping("/reset-all")
    public ResponseEntity<ApiResponse<String>> resetAll() {
        try {
            long completions = challengeCompletionRepository.count();
            long badges = userBadgeRepository.count();
            long progress = userProgressRepository.count();
            
            challengeCompletionRepository.deleteAll();
            userBadgeRepository.deleteAll();
            userProgressRepository.deleteAll();
            
            String message = String.format(
                "リセット完了: 達成記録 %d件, バッジ %d件, 進捗 %d件を削除しました",
                completions, badges, progress
            );
            
            return ResponseEntity.ok(ApiResponse.success(message));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("リセットに失敗しました: " + e.getMessage()));
        }
    }
    
    /**
     * バッジのみリセット（開発用）
     * DELETE /api/debug/reset-badges
     */
    @DeleteMapping("/reset-badges")
    public ResponseEntity<ApiResponse<String>> resetBadges() {
        try {
            long count = userBadgeRepository.count();
            userBadgeRepository.deleteAll();
            
            String message = String.format("バッジ %d件を削除しました", count);
            return ResponseEntity.ok(ApiResponse.success(message));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("リセットに失敗しました: " + e.getMessage()));
        }
    }
    
    /**
     * データベースの状態を確認（開発用）
     * GET /api/debug/status
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Object>> getStatus() {
        try {
            long completions = challengeCompletionRepository.count();
            long badges = userBadgeRepository.count();
            long progress = userProgressRepository.count();
            
            var status = new java.util.HashMap<String, Object>();
            status.put("challenge_completions", completions);
            status.put("user_badges", badges);
            status.put("user_progress", progress);
            
            return ResponseEntity.ok(ApiResponse.success(status));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("ステータス取得に失敗しました: " + e.getMessage()));
        }
    }
}
