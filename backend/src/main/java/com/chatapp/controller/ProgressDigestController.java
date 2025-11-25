package com.chatapp.controller;

import com.chatapp.model.ProgressDigest;
import com.chatapp.model.User;
import com.chatapp.service.ProgressDigestService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/digests")
public class ProgressDigestController {

    private final ProgressDigestService digestService;

    public ProgressDigestController(ProgressDigestService digestService) {
        this.digestService = digestService;
    }

    /**
     * 現在の週のダイジェストを生成
     * マネージャー以上の権限が必要
     */
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @PostMapping("/weekly/current")
    public ResponseEntity<ProgressDigest> generateCurrentWeekDigest(
            @AuthenticationPrincipal User user) {
        ProgressDigest digest = digestService.generateCurrentWeekDigest(user, user.getTenant());
        return ResponseEntity.ok(digest);
    }

    /**
     * 現在の月のダイジェストを生成
     * マネージャー以上の権限が必要
     */
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @PostMapping("/monthly/current")
    public ResponseEntity<ProgressDigest> generateCurrentMonthDigest(
            @AuthenticationPrincipal User user) {
        ProgressDigest digest = digestService.generateCurrentMonthDigest(user, user.getTenant());
        return ResponseEntity.ok(digest);
    }

    /**
     * 特定期間の週次ダイジェストを生成
     * マネージャー以上の権限が必要
     */
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @PostMapping("/weekly")
    public ResponseEntity<ProgressDigest> generateWeeklyDigest(
            @AuthenticationPrincipal User user,
            @RequestParam LocalDateTime periodStart) {
        ProgressDigest digest = digestService.generateWeeklyDigest(user, user.getTenant(), periodStart);
        return ResponseEntity.ok(digest);
    }

    /**
     * 特定期間の月次ダイジェストを生成
     * マネージャー以上の権限が必要
     */
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @PostMapping("/monthly")
    public ResponseEntity<ProgressDigest> generateMonthlyDigest(
            @AuthenticationPrincipal User user,
            @RequestParam LocalDateTime periodStart) {
        ProgressDigest digest = digestService.generateMonthlyDigest(user, user.getTenant(), periodStart);
        return ResponseEntity.ok(digest);
    }

    /**
     * ユーザーのダイジェスト一覧を取得
     */
    @GetMapping
    public ResponseEntity<Page<ProgressDigest>> getUserDigests(
            @AuthenticationPrincipal User user,
            Pageable pageable) {
        Page<ProgressDigest> digests = digestService.getUserDigests(user, user.getTenant(), pageable);
        return ResponseEntity.ok(digests);
    }

    /**
     * 特定タイプのダイジェスト一覧を取得
     */
    @GetMapping("/type/{digestType}")
    public ResponseEntity<List<ProgressDigest>> getUserDigestsByType(
            @AuthenticationPrincipal User user,
            @PathVariable ProgressDigest.DigestType digestType) {
        List<ProgressDigest> digests = digestService.getUserDigestsByType(user, user.getTenant(), digestType);
        return ResponseEntity.ok(digests);
    }
}
