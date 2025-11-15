package com.chatapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Box種別（Box Type）エンティティ
 * 投稿Boxの種類を定義（上司Box、部下Box、同僚Box、PMBox など）
 */
@Entity
@Table(name = "box_types",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tenant_id", "box_name"})
    },
    indexes = {
        @Index(name = "idx_tenant", columnList = "tenant_id"),
        @Index(name = "idx_is_active", columnList = "is_active"),
        @Index(name = "idx_tenant_active", columnList = "tenant_id, is_active")
    }
)
public class BoxType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "box_name", nullable = false, length = 50)
    private String boxName;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 閲覧可能な最小権限レベル
     */
    @Column(name = "min_role_level_to_view", nullable = false)
    private Integer minRoleLevelToView = 20;

    /**
     * 階層的閲覧かどうか
     */
    @Column(name = "is_hierarchical", nullable = false)
    private Boolean isHierarchical = true;

    /**
     * 投稿可能な最小権限レベル
     */
    @Column(name = "min_role_level_to_post", nullable = false)
    private Integer minRoleLevelToPost = 20;

    /**
     * システム標準Box
     */
    @Column(name = "is_system_box", nullable = false)
    private Boolean isSystemBox = false;

    /**
     * アクティブ状態
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

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

    public String getBoxName() {
        return boxName;
    }

    public void setBoxName(String boxName) {
        this.boxName = boxName;
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

    public Integer getMinRoleLevelToView() {
        return minRoleLevelToView;
    }

    public void setMinRoleLevelToView(Integer minRoleLevelToView) {
        this.minRoleLevelToView = minRoleLevelToView;
    }

    public Boolean getIsHierarchical() {
        return isHierarchical;
    }

    public void setIsHierarchical(Boolean isHierarchical) {
        this.isHierarchical = isHierarchical;
    }

    public Integer getMinRoleLevelToPost() {
        return minRoleLevelToPost;
    }

    public void setMinRoleLevelToPost(Integer minRoleLevelToPost) {
        this.minRoleLevelToPost = minRoleLevelToPost;
    }

    public Boolean getIsSystemBox() {
        return isSystemBox;
    }

    public void setIsSystemBox(Boolean isSystemBox) {
        this.isSystemBox = isSystemBox;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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
     * ユーザーがこのBoxを閲覧可能かチェック
     */
    public boolean canView(Integer userRoleLevel) {
        return userRoleLevel >= this.minRoleLevelToView;
    }

    /**
     * ユーザーがこのBoxに投稿可能かチェック
     */
    public boolean canPost(Integer userRoleLevel) {
        return userRoleLevel >= this.minRoleLevelToPost;
    }
}
