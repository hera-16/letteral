package com.chatapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 権限昇格申請テーブル
 * ユーザーが上位権限への昇格を申請する
 */
@Entity
@Table(name = "role_promotion_requests",
    indexes = {
        @Index(name = "idx_rpr_tenant", columnList = "tenant_id"),
        @Index(name = "idx_rpr_user", columnList = "user_id"),
        @Index(name = "idx_rpr_organization", columnList = "organization_id"),
        @Index(name = "idx_rpr_status", columnList = "status"),
        @Index(name = "idx_rpr_tenant_status", columnList = "tenant_id, status"),
        @Index(name = "idx_rpr_created_at", columnList = "created_at")
    }
)
public class RolePromotionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "current_role_level", nullable = false)
    private Integer currentRoleLevel;

    @Column(name = "requested_role_level", nullable = false)
    private Integer requestedRoleLevel;

    @Column(name = "requested_role_name", nullable = false, length = 50)
    private String requestedRoleName;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "reviewed_by")
    private Long reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "review_comment", columnDefinition = "TEXT")
    private String reviewComment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", insertable = false, updatable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by", insertable = false, updatable = false)
    private User reviewer;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public Integer getCurrentRoleLevel() {
        return currentRoleLevel;
    }

    public void setCurrentRoleLevel(Integer currentRoleLevel) {
        this.currentRoleLevel = currentRoleLevel;
    }

    public Integer getRequestedRoleLevel() {
        return requestedRoleLevel;
    }

    public void setRequestedRoleLevel(Integer requestedRoleLevel) {
        this.requestedRoleLevel = requestedRoleLevel;
    }

    public String getRequestedRoleName() {
        return requestedRoleName;
    }

    public void setRequestedRoleName(String requestedRoleName) {
        this.requestedRoleName = requestedRoleName;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(Long reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getReviewComment() {
        return reviewComment;
    }

    public void setReviewComment(String reviewComment) {
        this.reviewComment = reviewComment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
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

    public User getReviewer() {
        return reviewer;
    }

    public void setReviewer(User reviewer) {
        this.reviewer = reviewer;
    }

    /**
     * 申請を承認
     */
    public void approve(Long reviewerId, String comment) {
        this.status = "APPROVED";
        this.reviewedBy = reviewerId;
        this.reviewedAt = LocalDateTime.now();
        this.reviewComment = comment;
    }

    /**
     * 申請を却下
     */
    public void reject(Long reviewerId, String comment) {
        this.status = "REJECTED";
        this.reviewedBy = reviewerId;
        this.reviewedAt = LocalDateTime.now();
        this.reviewComment = comment;
    }

    /**
     * 申請をキャンセル
     */
    public void cancel() {
        this.status = "CANCELLED";
    }

    /**
     * 申請が保留中かチェック
     */
    public boolean isPending() {
        return "PENDING".equals(this.status);
    }

    /**
     * 申請が承認済みかチェック
     */
    public boolean isApproved() {
        return "APPROVED".equals(this.status);
    }
}
