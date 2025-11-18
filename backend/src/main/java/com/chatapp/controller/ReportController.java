package com.chatapp.controller;

import com.chatapp.model.Report;
import com.chatapp.model.User;
import com.chatapp.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * 通報を作成
     */
    @PostMapping
    public ResponseEntity<Report> createReport(
            @AuthenticationPrincipal User user,
            @RequestBody CreateReportRequest request) {
        Report report = reportService.createReport(
                user.getTenantId(),
                user.getId(),
                request.getReportType(),
                request.getReason(),
                request.getReportedUserId(),
                request.getReportedPostId(),
                request.getReportedMessageId()
        );
        return ResponseEntity.ok(report);
    }

    /**
     * テナントの通報一覧を取得
     */
    @GetMapping
    public ResponseEntity<Page<Report>> getReports(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Report> reports = reportService.getReportsByTenant(user.getTenantId(), pageable);
        return ResponseEntity.ok(reports);
    }

    /**
     * ステータス別に通報を取得
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<Report>> getReportsByStatus(
            @AuthenticationPrincipal User user,
            @PathVariable Report.ReportStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Report> reports = reportService.getReportsByStatus(user.getTenantId(), status, pageable);
        return ResponseEntity.ok(reports);
    }

    /**
     * 保留中の通報を取得
     */
    @GetMapping("/pending")
    public ResponseEntity<List<Report>> getPendingReports(@AuthenticationPrincipal User user) {
        List<Report> reports = reportService.getPendingReports(user.getTenantId());
        return ResponseEntity.ok(reports);
    }

    /**
     * 通報の詳細を取得
     */
    @GetMapping("/{id}")
    public ResponseEntity<Report> getReport(@PathVariable Long id) {
        Report report = reportService.getReportById(id);
        return ResponseEntity.ok(report);
    }

    /**
     * 通報のステータスを更新
     */
    @PutMapping("/{id}/resolve")
    public ResponseEntity<Report> resolveReport(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @RequestBody ResolveReportRequest request) {
        Report report = reportService.updateReportStatus(
                id,
                user.getId(),
                request.getStatus(),
                request.getResolution()
        );
        return ResponseEntity.ok(report);
    }

    /**
     * 通報統計を取得
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics(@AuthenticationPrincipal User user) {
        Map<String, Object> stats = reportService.getReportStatistics(user.getTenantId());
        return ResponseEntity.ok(stats);
    }

    // DTOクラス
    public static class CreateReportRequest {
        private Report.ReportType reportType;
        private String reason;
        private Long reportedUserId;
        private Long reportedPostId;
        private Long reportedMessageId;

        public Report.ReportType getReportType() { return reportType; }
        public void setReportType(Report.ReportType reportType) { this.reportType = reportType; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public Long getReportedUserId() { return reportedUserId; }
        public void setReportedUserId(Long reportedUserId) { this.reportedUserId = reportedUserId; }
        public Long getReportedPostId() { return reportedPostId; }
        public void setReportedPostId(Long reportedPostId) { this.reportedPostId = reportedPostId; }
        public Long getReportedMessageId() { return reportedMessageId; }
        public void setReportedMessageId(Long reportedMessageId) { this.reportedMessageId = reportedMessageId; }
    }

    public static class ResolveReportRequest {
        private Report.ReportStatus status;
        private String resolution;

        public Report.ReportStatus getStatus() { return status; }
        public void setStatus(Report.ReportStatus status) { this.status = status; }
        public String getResolution() { return resolution; }
        public void setResolution(String resolution) { this.resolution = resolution; }
    }
}
