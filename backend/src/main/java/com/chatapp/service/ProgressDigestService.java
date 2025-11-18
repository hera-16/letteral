package com.chatapp.service;

import com.chatapp.model.*;
import com.chatapp.repository.ProgressDigestRepository;
import com.chatapp.repository.ProgressPostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProgressDigestService {

    private final ProgressDigestRepository digestRepository;
    private final ProgressPostRepository progressPostRepository;

    public ProgressDigestService(ProgressDigestRepository digestRepository,
                                ProgressPostRepository progressPostRepository) {
        this.digestRepository = digestRepository;
        this.progressPostRepository = progressPostRepository;
    }

    /**
     * 週次ダイジェストを生成
     */
    @Transactional
    public ProgressDigest generateWeeklyDigest(User user, Tenant tenant, LocalDateTime periodStart) {
        LocalDateTime periodEnd = periodStart.plusWeeks(1);

        // 既存チェック
        if (digestRepository.existsByUserAndTenantAndDigestTypeAndPeriodStartAndPeriodEnd(
                user, tenant, ProgressDigest.DigestType.WEEKLY, periodStart, periodEnd)) {
            return digestRepository.findByUserAndTenantAndDigestTypeAndPeriodStartAndPeriodEnd(
                    user, tenant, ProgressDigest.DigestType.WEEKLY, periodStart, periodEnd)
                    .orElseThrow(() -> new RuntimeException("Failed to retrieve digest"));
        }

        // 期間内の進捗投稿を取得
        List<ProgressPost> posts = progressPostRepository.findByAuthorAndCreatedAtBetween(
                user, periodStart, periodEnd);

        ProgressDigest digest = new ProgressDigest();
        digest.setUser(user);
        digest.setTenant(tenant);
        digest.setDigestType(ProgressDigest.DigestType.WEEKLY);
        digest.setPeriodStart(periodStart);
        digest.setPeriodEnd(periodEnd);

        // 統計情報を計算
        digest.setTotalPosts(posts.size());

        long completedGoals = posts.stream()
                .filter(p -> p.getNextAction() != null && !p.getNextAction().isEmpty())
                .count();
        digest.setCompletedGoals((int) completedGoals);

        long ongoingGoals = posts.stream()
                .filter(p -> p.getContent() != null && !p.getContent().isEmpty())
                .count();
        digest.setOngoingGoals((int) ongoingGoals);

        long challengesCount = posts.stream()
                .filter(p -> p.getBlockers() != null && !p.getBlockers().isEmpty())
                .count();
        digest.setChallenges((int) challengesCount);

        // トップの達成・課題・学びを抽出
        String topAchievements = posts.stream()
                .map(ProgressPost::getContent)
                .filter(a -> a != null && !a.isEmpty())
                .limit(5)
                .collect(Collectors.joining("\n• ", "• ", ""));
        digest.setTopAchievements(topAchievements.isEmpty() ? "なし" : topAchievements);

        String topChallenges = posts.stream()
                .map(ProgressPost::getBlockers)
                .filter(c -> c != null && !c.isEmpty())
                .limit(5)
                .collect(Collectors.joining("\n• ", "• ", ""));
        digest.setTopChallenges(topChallenges.isEmpty() ? "なし" : topChallenges);

        String keyLearnings = posts.stream()
                .map(ProgressPost::getLearnings)
                .filter(l -> l != null && !l.isEmpty())
                .limit(5)
                .collect(Collectors.joining("\n• ", "• ", ""));
        digest.setKeyLearnings(keyLearnings.isEmpty() ? "なし" : keyLearnings);

        // サマリー生成
        String summary = generateSummary(digest);
        digest.setSummary(summary);

        // 次のステップの提案
        String nextSteps = generateNextSteps(posts);
        digest.setNextSteps(nextSteps);

        return digestRepository.save(digest);
    }

    /**
     * 月次ダイジェストを生成
     */
    @Transactional
    public ProgressDigest generateMonthlyDigest(User user, Tenant tenant, LocalDateTime periodStart) {
        LocalDateTime periodEnd = periodStart.plusMonths(1);

        // 既存チェック
        if (digestRepository.existsByUserAndTenantAndDigestTypeAndPeriodStartAndPeriodEnd(
                user, tenant, ProgressDigest.DigestType.MONTHLY, periodStart, periodEnd)) {
            return digestRepository.findByUserAndTenantAndDigestTypeAndPeriodStartAndPeriodEnd(
                    user, tenant, ProgressDigest.DigestType.MONTHLY, periodStart, periodEnd)
                    .orElseThrow(() -> new RuntimeException("Failed to retrieve digest"));
        }

        List<ProgressPost> posts = progressPostRepository.findByAuthorAndCreatedAtBetween(
                user, periodStart, periodEnd);

        ProgressDigest digest = new ProgressDigest();
        digest.setUser(user);
        digest.setTenant(tenant);
        digest.setDigestType(ProgressDigest.DigestType.MONTHLY);
        digest.setPeriodStart(periodStart);
        digest.setPeriodEnd(periodEnd);

        // 統計情報を計算（週次と同様）
        digest.setTotalPosts(posts.size());

        long completedGoals = posts.stream()
                .filter(p -> p.getNextAction() != null && !p.getNextAction().isEmpty())
                .count();
        digest.setCompletedGoals((int) completedGoals);

        long ongoingGoals = posts.stream()
                .filter(p -> p.getContent() != null && !p.getContent().isEmpty())
                .count();
        digest.setOngoingGoals((int) ongoingGoals);

        long challengesCount = posts.stream()
                .filter(p -> p.getBlockers() != null && !p.getBlockers().isEmpty())
                .count();
        digest.setChallenges((int) challengesCount);

        String topAchievements = posts.stream()
                .map(ProgressPost::getContent)
                .filter(a -> a != null && !a.isEmpty())
                .limit(10)
                .collect(Collectors.joining("\n• ", "• ", ""));
        digest.setTopAchievements(topAchievements.isEmpty() ? "なし" : topAchievements);

        String topChallenges = posts.stream()
                .map(ProgressPost::getBlockers)
                .filter(c -> c != null && !c.isEmpty())
                .limit(10)
                .collect(Collectors.joining("\n• ", "• ", ""));
        digest.setTopChallenges(topChallenges.isEmpty() ? "なし" : topChallenges);

        String keyLearnings = posts.stream()
                .map(ProgressPost::getLearnings)
                .filter(l -> l != null && !l.isEmpty())
                .limit(10)
                .collect(Collectors.joining("\n• ", "• ", ""));
        digest.setKeyLearnings(keyLearnings.isEmpty() ? "なし" : keyLearnings);

        String summary = generateSummary(digest);
        digest.setSummary(summary);

        String nextSteps = generateNextSteps(posts);
        digest.setNextSteps(nextSteps);

        return digestRepository.save(digest);
    }

    /**
     * 現在の週のダイジェストを自動生成
     */
    @Transactional
    public ProgressDigest generateCurrentWeekDigest(User user, Tenant tenant) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekStart = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .withHour(0).withMinute(0).withSecond(0).withNano(0);
        return generateWeeklyDigest(user, tenant, weekStart);
    }

    /**
     * 現在の月のダイジェストを自動生成
     */
    @Transactional
    public ProgressDigest generateCurrentMonthDigest(User user, Tenant tenant) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthStart = now.with(TemporalAdjusters.firstDayOfMonth())
                .withHour(0).withMinute(0).withSecond(0).withNano(0);
        return generateMonthlyDigest(user, tenant, monthStart);
    }

    /**
     * ユーザーのダイジェスト一覧を取得
     */
    @Transactional(readOnly = true)
    public Page<ProgressDigest> getUserDigests(User user, Tenant tenant, Pageable pageable) {
        return digestRepository.findByUserAndTenantOrderByPeriodStartDesc(user, tenant, pageable);
    }

    /**
     * 特定タイプのダイジェスト一覧を取得
     */
    @Transactional(readOnly = true)
    public List<ProgressDigest> getUserDigestsByType(
            User user, Tenant tenant, ProgressDigest.DigestType digestType) {
        return digestRepository.findByUserAndTenantAndDigestTypeOrderByPeriodStartDesc(
                user, tenant, digestType);
    }

    /**
     * サマリーテキストを生成
     */
    private String generateSummary(ProgressDigest digest) {
        StringBuilder summary = new StringBuilder();
        summary.append(String.format("期間: %s ~ %s\n\n",
                digest.getPeriodStart().toLocalDate(),
                digest.getPeriodEnd().toLocalDate()));
        summary.append(String.format("総投稿数: %d件\n", digest.getTotalPosts()));
        summary.append(String.format("達成した目標: %d件\n", digest.getCompletedGoals()));
        summary.append(String.format("進行中の取り組み: %d件\n", digest.getOngoingGoals()));
        summary.append(String.format("課題: %d件\n", digest.getChallenges()));

        return summary.toString();
    }

    /**
     * 次のステップの提案を生成
     */
    private String generateNextSteps(List<ProgressPost> posts) {
        // 課題から次のステップを抽出
        List<String> blockers = posts.stream()
                .map(ProgressPost::getBlockers)
                .filter(c -> c != null && !c.isEmpty())
                .distinct()
                .limit(3)
                .collect(Collectors.toList());

        if (blockers.isEmpty()) {
            return "引き続き現在の取り組みを継続してください。";
        }

        StringBuilder nextSteps = new StringBuilder("以下の課題に取り組むことを推奨します:\n\n");
        for (int i = 0; i < blockers.size(); i++) {
            nextSteps.append(String.format("%d. %s\n", i + 1, blockers.get(i)));
        }

        return nextSteps.toString();
    }
}
