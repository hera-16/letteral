package com.chatapp.service;

import com.chatapp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final ProgressPostRepository progressPostRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ReportRepository reportRepository;
    private final GroupRepository groupRepository;

    @Autowired
    public AdminDashboardService(UserRepository userRepository,
                                 ProgressPostRepository progressPostRepository,
                                 ChatMessageRepository chatMessageRepository,
                                 ReportRepository reportRepository,
                                 GroupRepository groupRepository) {
        this.userRepository = userRepository;
        this.progressPostRepository = progressPostRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.reportRepository = reportRepository;
        this.groupRepository = groupRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardStatistics(Long tenantId) {
        Map<String, Object> stats = new HashMap<>();

        // ユーザー統計
        Map<String, Object> userStats = new HashMap<>();
        Long totalUsers = userRepository.countByTenantId(tenantId);
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        // アクティブユーザー数（簡易版: 30日以内にログインしたユーザー）
        userStats.put("total", totalUsers);
        userStats.put("active", totalUsers); // TODO: 実際のアクティブユーザー計算
        stats.put("users", userStats);

        // 進捗投稿統計
        Map<String, Object> postStats = new HashMap<>();
        Long totalPosts = progressPostRepository.countByTenantId(tenantId);
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        Long recentPosts = progressPostRepository.countByTenantIdAndCreatedAtAfter(tenantId, sevenDaysAgo);
        postStats.put("total", totalPosts);
        postStats.put("recentWeek", recentPosts);
        stats.put("posts", postStats);

        // メッセージ統計
        Map<String, Object> messageStats = new HashMap<>();
        Long totalMessages = chatMessageRepository.countByTenantId(tenantId);
        messageStats.put("total", totalMessages);
        stats.put("messages", messageStats);

        // OKR統計（将来の実装用にプレースホルダーを用意）
        Map<String, Object> okrStats = new HashMap<>();
        okrStats.put("total", 0L);
        okrStats.put("completed", 0L);
        okrStats.put("inProgress", 0L);
        okrStats.put("completionRate", 0.0);
        stats.put("okrs", okrStats);

        // 通報統計
        Map<String, Object> reportStats = new HashMap<>();
        Long pendingReports = reportRepository.countByTenantIdAndStatus(tenantId, com.chatapp.model.Report.ReportStatus.PENDING);
        reportStats.put("pending", pendingReports);
        stats.put("reports", reportStats);

        // グループ統計
        Map<String, Object> groupStats = new HashMap<>();
        Long totalGroups = groupRepository.countByTenantId(tenantId);
        groupStats.put("total", totalGroups);
        stats.put("groups", groupStats);

        return stats;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getUserGrowthData(Long tenantId, int days) {
        Map<String, Object> growthData = new HashMap<>();

        // TODO: 日別のユーザー登録数を集計
        // 簡易版として現在の合計を返す
        Long totalUsers = userRepository.countByTenantId(tenantId);
        growthData.put("total", totalUsers);
        growthData.put("days", days);

        return growthData;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getActivityTrends(Long tenantId, int days) {
        Map<String, Object> trends = new HashMap<>();
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);

        // 投稿数トレンド
        Long recentPosts = progressPostRepository.countByTenantIdAndCreatedAtAfter(tenantId, startDate);
        trends.put("posts", recentPosts);

        // TODO: 日別のアクティビティデータを集計
        trends.put("days", days);

        return trends;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getTopContributors(Long tenantId, int limit) {
        Map<String, Object> contributors = new HashMap<>();

        // TODO: 投稿数やメッセージ数でユーザーをランキング
        // 簡易版として空のリストを返す
        contributors.put("limit", limit);

        return contributors;
    }
}
