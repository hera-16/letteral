package com.chatapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * ユーザー権限階級テーブル
 * ユーザーごとに権限階級を割り当て
 */
@Entity
@Table(name = "user_roles",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "organization_id"})
    },
    indexes = {
        @Index(name = "idx_ur_tenant", columnList = "tenant_id"),
        @Index(name = "idx_ur_user", columnList = "user_id"),
        @Index(name = "idx_ur_organization", columnList = "organization_id"),
        @Index(name = "idx_ur_role_level", columnList = "role_level"),
        @Index(name = "idx_ur_approval_status", columnList = "approval_status"),
        @Index(name = "idx_ur_tenant_org", columnList = "tenant_id, organization_id")
    }
)
public class UserRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_name", nullable = false, length = 50)
    private RoleName roleName;

    @Column(name = "role_level", nullable = false)
    private Integer roleLevel;

    @Column(name = "total_posts", nullable = false)
    private Integer totalPosts = 0;

    @Column(name = "days_active", nullable = false)
    private Integer daysActive = 0;

    @Column(name = "approval_status", nullable = false, length = 20)
    private String approvalStatus = "APPROVED";

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "assigned_at", nullable = false, updatable = false)
    private LocalDateTime assignedAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", insertable = false, updatable = false)
    private Organization organization;

    @PrePersist
    protected void onCreate() {
        assignedAt = LocalDateTime.now();
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

    public RoleName getRoleName() {
        return roleName;
    }

    public void setRoleName(RoleName roleName) {
        this.roleName = roleName;
    }

    public Integer getRoleLevel() {
        return roleLevel;
    }

    public void setRoleLevel(Integer roleLevel) {
        this.roleLevel = roleLevel;
    }

    public Integer getTotalPosts() {
        return totalPosts;
    }

    public void setTotalPosts(Integer totalPosts) {
        this.totalPosts = totalPosts;
    }

    public Integer getDaysActive() {
        return daysActive;
    }

    public void setDaysActive(Integer daysActive) {
        this.daysActive = daysActive;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public Long getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(Long approvedBy) {
        this.approvedBy = approvedBy;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
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

    /**
     * 昇格条件を満たしているかチェック
     */
    public boolean meetsPromotionRequirements(Integer requiredPosts, Integer requiredDays) {
        return this.totalPosts >= requiredPosts && this.daysActive >= requiredDays;
    }

    /**
     * 投稿数を増やす
     */
    public void incrementPostCount() {
        this.totalPosts++;
    }

    /**
     * アクティブ日数を増やす
     */
    public void incrementDaysActive() {
        this.daysActive++;
    }
}
