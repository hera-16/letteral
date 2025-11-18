package com.chatapp.controller;

import com.chatapp.model.ProgressDigest;
import com.chatapp.model.User;
import com.chatapp.repository.ProgressDigestRepository;
import com.chatapp.service.ExportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/export")
public class ExportController {

    private final ExportService exportService;
    private final ProgressDigestRepository digestRepository;

    public ExportController(ExportService exportService,
                           ProgressDigestRepository digestRepository) {
        this.exportService = exportService;
        this.digestRepository = digestRepository;
    }

    /**
     * 進捗投稿をCSVでエクスポート
     * マネージャー以上の権限が必要
     *
     * @param startDate 開始日時（オプション、デフォルト: 1ヶ月前）
     * @param endDate 終了日時（オプション、デフォルト: 現在）
     */
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @GetMapping("/progress/csv")
    public ResponseEntity<byte[]> exportProgressToCSV(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) throws IOException {

        // デフォルト値の設定
        if (startDate == null) {
            startDate = LocalDateTime.now().minusMonths(1);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }

        byte[] csvData = exportService.exportProgressPostsToCSV(user, user.getTenant(), startDate, endDate);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String filename = String.format("progress_%s_%s.csv",
                startDate.format(formatter), endDate.format(formatter));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.setContentDispositionFormData("attachment", filename);
        headers.add("Content-Type", "text/csv; charset=UTF-8");

        return ResponseEntity.ok()
                .headers(headers)
                .body(csvData);
    }

    /**
     * ダイジェストをCSVでエクスポート
     * マネージャー以上の権限が必要
     */
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @GetMapping("/digest/{digestId}/csv")
    public ResponseEntity<byte[]> exportDigestToCSV(
            @PathVariable Long digestId) throws IOException {

        ProgressDigest digest = digestRepository.findById(digestId)
                .orElseThrow(() -> new RuntimeException("Digest not found"));

        byte[] csvData = exportService.exportDigestToCSV(digest);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String filename = String.format("digest_%s_%s.csv",
                digest.getDigestType().toString().toLowerCase(),
                digest.getPeriodStart().format(formatter));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.setContentDispositionFormData("attachment", filename);

        return ResponseEntity.ok()
                .headers(headers)
                .body(csvData);
    }

    /**
     * ダイジェストをMarkdownでエクスポート
     * マネージャー以上の権限が必要
     */
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @GetMapping("/digest/{digestId}/markdown")
    public ResponseEntity<byte[]> exportDigestToMarkdown(
            @PathVariable Long digestId) {

        ProgressDigest digest = digestRepository.findById(digestId)
                .orElseThrow(() -> new RuntimeException("Digest not found"));

        byte[] markdownData = exportService.exportDigestToMarkdown(digest);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String filename = String.format("digest_%s_%s.md",
                digest.getDigestType().toString().toLowerCase(),
                digest.getPeriodStart().format(formatter));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/markdown; charset=UTF-8"));
        headers.setContentDispositionFormData("attachment", filename);

        return ResponseEntity.ok()
                .headers(headers)
                .body(markdownData);
    }

    /**
     * 進捗投稿をExcelでエクスポート
     * マネージャー以上の権限が必要
     *
     * @param startDate 開始日時（オプション、デフォルト: 1ヶ月前）
     * @param endDate 終了日時（オプション、デフォルト: 現在）
     */
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @GetMapping("/progress/excel")
    public ResponseEntity<byte[]> exportProgressToExcel(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) throws IOException {

        // デフォルト値の設定
        if (startDate == null) {
            startDate = LocalDateTime.now().minusMonths(1);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }

        byte[] excelData = exportService.exportProgressPostsToExcel(user, user.getTenant(), startDate, endDate);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String filename = String.format("progress_%s_%s.xlsx",
                startDate.format(formatter), endDate.format(formatter));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", filename);

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelData);
    }

    /**
     * 1on1ミーティングをCSVでエクスポート
     * マネージャー以上の権限が必要
     *
     * @param startDate 開始日時（オプション、デフォルト: 3ヶ月前）
     * @param endDate 終了日時（オプション、デフォルト: 現在）
     */
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @GetMapping("/meetings/csv")
    public ResponseEntity<byte[]> exportMeetingsToCSV(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) throws IOException {

        // デフォルト値の設定（ミーティングは3ヶ月分）
        if (startDate == null) {
            startDate = LocalDateTime.now().minusMonths(3);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }

        byte[] csvData = exportService.exportMeetingsToCSV(user, user.getTenant(), startDate, endDate);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String filename = String.format("meetings_%s_%s.csv",
                startDate.format(formatter), endDate.format(formatter));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.setContentDispositionFormData("attachment", filename);

        return ResponseEntity.ok()
                .headers(headers)
                .body(csvData);
    }

    /**
     * 1on1ミーティングをExcelでエクスポート
     * マネージャー以上の権限が必要
     *
     * @param startDate 開始日時（オプション、デフォルト: 3ヶ月前）
     * @param endDate 終了日時（オプション、デフォルト: 現在）
     */
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @GetMapping("/meetings/excel")
    public ResponseEntity<byte[]> exportMeetingsToExcel(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) throws IOException {

        // デフォルト値の設定（ミーティングは3ヶ月分）
        if (startDate == null) {
            startDate = LocalDateTime.now().minusMonths(3);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }

        byte[] excelData = exportService.exportMeetingsToExcel(user, user.getTenant(), startDate, endDate);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String filename = String.format("meetings_%s_%s.xlsx",
                startDate.format(formatter), endDate.format(formatter));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", filename);

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelData);
    }

    /**
     * ユーザー一覧をExcelでエクスポート（管理者専用）
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/excel")
    public ResponseEntity<byte[]> exportUsersToExcel(
            @AuthenticationPrincipal User user) throws IOException {

        byte[] excelData = exportService.exportUsersToExcel(user.getTenant());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String filename = String.format("users_%s.xlsx",
                LocalDateTime.now().format(formatter));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", filename);

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelData);
    }
}
