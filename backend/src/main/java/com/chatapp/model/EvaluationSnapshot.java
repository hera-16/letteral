package com.chatapp.model;

import com.chatapp.model.enums.ExportFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 評価スナップショットエンティティ
 * 評価期間ごとの成果スナップショット
 */
@Entity
@Table(name = "evaluation_snapshots")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class EvaluationSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Size(max = 100)
    @Column(name = "period_label", length = 100)
    private String periodLabel;

    @Column(name = "total_posts", nullable = false)
    private Integer totalPosts = 0;

    @Column(name = "total_achievements", nullable = false)
    private Integer totalAchievements = 0;

    @Column(name = "avg_achievement_rate", precision = 5, scale = 2)
    private BigDecimal avgAchievementRate;

    @Column(name = "total_reactions_received", nullable = false)
    private Integer totalReactionsReceived = 0;

    @Column(name = "total_okr_contributions", nullable = false)
    private Integer totalOkrContributions = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "snapshot_data", columnDefinition = "JSON")
    private Map<String, Object> snapshotData;

    @Column(name = "exported_at")
    private LocalDateTime exportedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "export_format", length = 20)
    private ExportFormat exportFormat;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // コンストラクタ
    public EvaluationSnapshot() {
        this.createdAt = LocalDateTime.now();
    }

    public EvaluationSnapshot(Tenant tenant, User user, Organization organization, LocalDate periodStart, LocalDate periodEnd) {
        this();
        this.tenant = tenant;
        this.user = user;
        this.organization = organization;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(LocalDate periodStart) {
        this.periodStart = periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(LocalDate periodEnd) {
        this.periodEnd = periodEnd;
    }

    public String getPeriodLabel() {
        return periodLabel;
    }

    public void setPeriodLabel(String periodLabel) {
        this.periodLabel = periodLabel;
    }

    public Integer getTotalPosts() {
        return totalPosts;
    }

    public void setTotalPosts(Integer totalPosts) {
        this.totalPosts = totalPosts;
    }

    public Integer getTotalAchievements() {
        return totalAchievements;
    }

    public void setTotalAchievements(Integer totalAchievements) {
        this.totalAchievements = totalAchievements;
    }

    public BigDecimal getAvgAchievementRate() {
        return avgAchievementRate;
    }

    public void setAvgAchievementRate(BigDecimal avgAchievementRate) {
        this.avgAchievementRate = avgAchievementRate;
    }

    public Integer getTotalReactionsReceived() {
        return totalReactionsReceived;
    }

    public void setTotalReactionsReceived(Integer totalReactionsReceived) {
        this.totalReactionsReceived = totalReactionsReceived;
    }

    public Integer getTotalOkrContributions() {
        return totalOkrContributions;
    }

    public void setTotalOkrContributions(Integer totalOkrContributions) {
        this.totalOkrContributions = totalOkrContributions;
    }

    public Map<String, Object> getSnapshotData() {
        return snapshotData;
    }

    public void setSnapshotData(Map<String, Object> snapshotData) {
        this.snapshotData = snapshotData;
    }

    public LocalDateTime getExportedAt() {
        return exportedAt;
    }

    public void setExportedAt(LocalDateTime exportedAt) {
        this.exportedAt = exportedAt;
    }

    public ExportFormat getExportFormat() {
        return exportFormat;
    }

    public void setExportFormat(ExportFormat exportFormat) {
        this.exportFormat = exportFormat;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
