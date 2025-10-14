package com.chatapp.controller;

import com.chatapp.dto.ApiResponse;
import com.chatapp.model.ChallengeCompletion;
import com.chatapp.model.DailyChallenge;
import com.chatapp.model.User;
import com.chatapp.model.UserProgress;
import com.chatapp.repository.UserRepository;
import com.chatapp.service.ChallengeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * デイリーチャレンジのREST APIコントローラー
 */
@RestController
@RequestMapping("/api/challenges")
public class ChallengeController {
    
    private final ChallengeService challengeService;
    private final UserRepository userRepository;
    
    public ChallengeController(ChallengeService challengeService, UserRepository userRepository) {
        this.challengeService = challengeService;
        this.userRepository = userRepository;
    }
    
    /**
     * 今日のおすすめチャレンジを取得
     * GET /api/challenges/today
     */
    @GetMapping("/today")
    public ResponseEntity<ApiResponse<List<DailyChallenge>>> getTodayChallenges(Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません"));
            
            List<DailyChallenge> challenges = challengeService.getTodayRecommendedChallenges(user.getId());
            
            return ResponseEntity.ok(ApiResponse.success(challenges));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("今日のチャレンジの取得に失敗しました: " + e.getMessage()));
        }
    }
    
    /**
     * タイプ別のチャレンジを取得
     * GET /api/challenges/by-type/{type}
     */
    @GetMapping("/by-type/{type}")
    public ResponseEntity<ApiResponse<List<DailyChallenge>>> getChallengesByType(
            @PathVariable String type) {
        try {
            DailyChallenge.ChallengeType challengeType = DailyChallenge.ChallengeType.valueOf(type.toUpperCase());
            List<DailyChallenge> challenges = challengeService.getChallengesByType(challengeType);
            
            return ResponseEntity.ok(ApiResponse.success(challenges));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("無効なチャレンジタイプです: " + type));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("チャレンジの取得に失敗しました: " + e.getMessage()));
        }
    }
    
    /**
     * チャレンジを達成する
     * POST /api/challenges/{challengeId}/complete
     */
    @PostMapping("/{challengeId}/complete")
    public ResponseEntity<ApiResponse<Map<String, Object>>> completeChallenge(
            @PathVariable Long challengeId,
            @RequestBody(required = false) Map<String, String> request,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません"));
            
            String note = request != null ? request.get("note") : null;
            
            ChallengeCompletion completion = challengeService.completeChallenge(user.getId(), challengeId, note);
            UserProgress progress = challengeService.getUserProgress(user.getId());
            
            // レスポンスデータを構築
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("completion", completion);
            responseData.put("progress", progress);
            responseData.put("flowerEmoji", progress.getFlowerEmoji());
            responseData.put("message", "チャレンジ達成おめでとうございます! +" + completion.getPointsEarned() + "ポイント");
            
            // レベルアップした場合は特別なメッセージ
            if (completion.getPointsEarned() >= 10) {
                int oldLevel = (progress.getTotalPoints() - completion.getPointsEarned()) / 100 + 1;
                int newLevel = progress.getFlowerLevel();
                if (newLevel > oldLevel) {
                    responseData.put("levelUpMessage", "レベルアップ! 花レベル " + newLevel + " になりました! " + progress.getFlowerEmoji());
                }
            }
            
            return ResponseEntity.ok(ApiResponse.success(responseData));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("チャレンジの達成に失敗しました: " + e.getMessage()));
        }
    }
    
    /**
     * 自分の進捗情報を取得
     * GET /api/challenges/progress
     */
    @GetMapping("/progress")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProgress(Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません"));
            
            UserProgress progress = challengeService.getUserProgress(user.getId());
            int todayCount = challengeService.getTodayCompletedCount(user.getId());
            
            Map<String, Object> progressData = new HashMap<>();
            progressData.put("progress", progress);
            progressData.put("flowerEmoji", progress.getFlowerEmoji());
            progressData.put("todayCompletedCount", todayCount);
            progressData.put("pointsToNextLevel", 100 - (progress.getTotalPoints() % 100));
            progressData.put("progressPercentage", (progress.getTotalPoints() % 100));
            
            return ResponseEntity.ok(ApiResponse.success(progressData));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("進捗情報の取得に失敗しました: " + e.getMessage()));
        }
    }
    
    /**
     * チャレンジ達成履歴を取得
     * GET /api/challenges/history
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<ChallengeCompletion>>> getHistory(Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません"));
            
            List<ChallengeCompletion> history = challengeService.getUserChallengeHistory(user.getId());
            
            return ResponseEntity.ok(ApiResponse.success(history));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("履歴の取得に失敗しました: " + e.getMessage()));
        }
    }
    
    /**
     * ランキングを取得
     * GET /api/challenges/ranking
     */
    @GetMapping("/ranking")
    public ResponseEntity<ApiResponse<List<UserProgress>>> getRanking(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<UserProgress> ranking = challengeService.getTopRanking(limit);
            
            return ResponseEntity.ok(ApiResponse.success(ranking));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("ランキングの取得に失敗しました: " + e.getMessage()));
        }
    }
    
    /**
     * 統計情報を取得
     * GET /api/challenges/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats(Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("ユーザーが見つかりません"));
            
            UserProgress progress = challengeService.getUserProgress(user.getId());
            
            // 今週の達成数
            LocalDateTime startOfWeek = LocalDateTime.now().minusDays(7);
            long weekCount = challengeService.countCompletionsBetween(user.getId(), startOfWeek, LocalDateTime.now());
            
            // 今月の達成数
            LocalDateTime startOfMonth = LocalDateTime.now().minusDays(30);
            long monthCount = challengeService.countCompletionsBetween(user.getId(), startOfMonth, LocalDateTime.now());
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalPoints", progress.getTotalPoints());
            stats.put("flowerLevel", progress.getFlowerLevel());
            stats.put("currentStreak", progress.getCurrentStreak());
            stats.put("longestStreak", progress.getLongestStreak());
            stats.put("weekCount", weekCount);
            stats.put("monthCount", monthCount);
            stats.put("flowerEmoji", progress.getFlowerEmoji());
            
            return ResponseEntity.ok(ApiResponse.success(stats));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("統計情報の取得に失敗しました: " + e.getMessage()));
        }
    }
}
