package com.chatapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 権限階級（Role Hierarchy）エンティティ
 * 社長 > 部長 > 課長 > PM > 一般 の階層構造を定義
 */
@Entity
@Table(name = "role_hierarchy",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tenant_id", "role_name"}),
        @UniqueConstraint(columnNames = {"tenant_id", "role_level"})
    },
    indexes = {
        @Index(name = "idx_rh_tenant", columnList = "tenant_id"),
        @Index(name = "idx_rh_role_level", columnList = "role_level"),
        @Index(name = "idx_rh_tenant_level", columnList = "tenant_id, role_level")
    }
)
public class RoleHierarchy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 所属テナント
     */
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    /**
     * 役職名
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role_name", nullable = false, length = 50)
    private RoleName roleName;

    /**
     * 権限レベル（数字が大きいほど上位）
     */
    @Column(name = "role_level", nullable = false)
    private Integer roleLevel;

    /**
     * 表示名
     */
    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    /**
     * 説明
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 昇格に必要な最小投稿数
     */
    @Column(name = "min_posts_required", nullable = false)
    private Integer minPostsRequired = 0;

    /**
     * 昇格に必要な最小活動日数
     */
    @Column(name = "min_days_active", nullable = false)
    private Integer minDaysActive = 0;

    /**
     * 承認が必要かどうか
     */
    @Column(name = "requires_approval", nullable = false)
    private Boolean requiresApproval = false;

    /**
     * 閲覧可能な下位レベル（このレベル以上まで閲覧可能）
     */
    @Column(name = "can_view_down_to_level", nullable = false)
    private Integer canViewDownToLevel = 0;

    /**
     * 作成日時
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新日時
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public RoleHierarchy() {
    }

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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getMinPostsRequired() {
        return minPostsRequired;
    }

    public void setMinPostsRequired(Integer minPostsRequired) {
        this.minPostsRequired = minPostsRequired;
    }

    public Integer getMinDaysActive() {
        return minDaysActive;
    }

    public void setMinDaysActive(Integer minDaysActive) {
        this.minDaysActive = minDaysActive;
    }

    public Boolean getRequiresApproval() {
        return requiresApproval;
    }

    public void setRequiresApproval(Boolean requiresApproval) {
        this.requiresApproval = requiresApproval;
    }

    public Integer getCanViewDownToLevel() {
        return canViewDownToLevel;
    }

    public void setCanViewDownToLevel(Integer canViewDownToLevel) {
        this.canViewDownToLevel = canViewDownToLevel;
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

    /**
     * このロールが指定されたレベルのBoxを閲覧可能かどうか
     */
    public boolean canView(Integer targetLevel) {
        return targetLevel >= this.canViewDownToLevel;
    }

    /**
     * 昇格条件を満たしているかチェック
     */
    public boolean meetsPromotionRequirements(Integer currentPosts, Integer currentDaysActive) {
        return currentPosts >= this.minPostsRequired && currentDaysActive >= this.minDaysActive;
    }
}
