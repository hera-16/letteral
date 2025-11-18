package com.chatapp.service;

import com.chatapp.model.Report;
import com.chatapp.model.User;
import com.chatapp.repository.ReportRepository;
import com.chatapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final AdminActionLogService adminActionLogService;

    @Transactional
    public Report createReport(Long tenantId, Long reporterId, Report.ReportType reportType,
                               String reason, Long reportedUserId, Long reportedPostId, Long reportedMessageId) {
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new RuntimeException("Reporter not found"));

        Report report = new Report();
        report.setTenantId(tenantId);
        report.setReporter(reporter);
        report.setReportType(reportType);
        report.setReason(reason);
        report.setStatus(Report.ReportStatus.PENDING);

        if (reportedUserId != null) {
            User reportedUser = userRepository.findById(reportedUserId)
                    .orElseThrow(() -> new RuntimeException("Reported user not found"));
            report.setReportedUser(reportedUser);
        }
        if (reportedPostId != null) {
            report.setReportedPostId(reportedPostId);
        }
        if (reportedMessageId != null) {
            report.setReportedMessageId(reportedMessageId);
        }

        return reportRepository.save(report);
    }

    @Transactional(readOnly = true)
    public Page<Report> getReportsByTenant(Long tenantId, Pageable pageable) {
        return reportRepository.findByTenantId(tenantId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Report> getReportsByStatus(Long tenantId, Report.ReportStatus status, Pageable pageable) {
        return reportRepository.findByTenantIdAndStatus(tenantId, status, pageable);
    }

    @Transactional(readOnly = true)
    public List<Report> getPendingReports(Long tenantId) {
        return reportRepository.findByTenantIdAndStatusOrderByCreatedAtDesc(tenantId, Report.ReportStatus.PENDING);
    }

    @Transactional
    public Report updateReportStatus(Long reportId, Long adminUserId, Report.ReportStatus status, String resolution) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new RuntimeException("Admin user not found"));

        report.setStatus(status);
        report.setResolution(resolution);
        report.setResolvedBy(admin);
        report.setResolvedAt(LocalDateTime.now());

        Report savedReport = reportRepository.save(report);

        // 管理者アクションログを記録
        adminActionLogService.logAction(
                report.getTenantId(),
                adminUserId,
                "RESOLVE_REPORT",
                "Report",
                reportId,
                "Report resolved with status: " + status,
                null
        );

        return savedReport;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getReportStatistics(Long tenantId) {
        Map<String, Object> stats = new HashMap<>();

        // ステータス別カウント
        Long pendingCount = reportRepository.countByTenantIdAndStatus(tenantId, Report.ReportStatus.PENDING);
        Long reviewingCount = reportRepository.countByTenantIdAndStatus(tenantId, Report.ReportStatus.REVIEWING);
        Long resolvedCount = reportRepository.countByTenantIdAndStatus(tenantId, Report.ReportStatus.RESOLVED);
        Long dismissedCount = reportRepository.countByTenantIdAndStatus(tenantId, Report.ReportStatus.DISMISSED);

        stats.put("pending", pendingCount);
        stats.put("reviewing", reviewingCount);
        stats.put("resolved", resolvedCount);
        stats.put("dismissed", dismissedCount);
        stats.put("total", pendingCount + reviewingCount + resolvedCount + dismissedCount);

        // タイプ別カウント
        List<Object[]> typeStats = reportRepository.countReportsByType(tenantId);
        Map<String, Long> typeMap = new HashMap<>();
        for (Object[] row : typeStats) {
            typeMap.put(row[0].toString(), (Long) row[1]);
        }
        stats.put("byType", typeMap);

        // 最近の通報
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<Report> recentReports = reportRepository.findRecentReportsByStatus(
                tenantId, Report.ReportStatus.PENDING, sevenDaysAgo);
        stats.put("recentPendingCount", recentReports.size());

        return stats;
    }

    @Transactional(readOnly = true)
    public Report getReportById(Long reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
    }
}
