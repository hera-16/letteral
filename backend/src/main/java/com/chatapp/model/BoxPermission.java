package com.chatapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Box閲覧権限テーブル
 * ユーザーがどのBoxを閲覧できるかを管理
 */
@Entity
@Table(name = "box_permissions",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "box_type_id", "organization_id"})
    },
    indexes = {
        @Index(name = "idx_tenant", columnList = "tenant_id"),
        @Index(name = "idx_user", columnList = "user_id"),
        @Index(name = "idx_box_type", columnList = "box_type_id"),
        @Index(name = "idx_organization", columnList = "organization_id"),
        @Index(name = "idx_tenant_user", columnList = "tenant_id, user_id"),
        @Index(name = "idx_expires_at", columnList = "expires_at")
    }
)
public class BoxPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "box_type_id", nullable = false)
    private Long boxTypeId;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "can_view", nullable = false)
    private Boolean canView = false;

    @Column(name = "can_post", nullable = false)
    private Boolean canPost = false;

    @Column(name = "can_reply", nullable = false)
    private Boolean canReply = false;

    @Column(name = "can_moderate", nullable = false)
    private Boolean canModerate = false;

    @Column(name = "granted_by", nullable = false, length = 50)
    private String grantedBy = "AUTO";

    @Column(name = "granted_at", nullable = false, updatable = false)
    private LocalDateTime grantedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "box_type_id", insertable = false, updatable = false)
    private BoxType boxType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", insertable = false, updatable = false)
    private Organization organization;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (grantedAt == null) {
            grantedAt = LocalDateTime.now();
        }
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

    public Long getBoxTypeId() {
        return boxTypeId;
    }

    public void setBoxTypeId(Long boxTypeId) {
        this.boxTypeId = boxTypeId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public Boolean getCanView() {
        return canView;
    }

    public void setCanView(Boolean canView) {
        this.canView = canView;
    }

    public Boolean getCanPost() {
        return canPost;
    }

    public void setCanPost(Boolean canPost) {
        this.canPost = canPost;
    }

    public Boolean getCanReply() {
        return canReply;
    }

    public void setCanReply(Boolean canReply) {
        this.canReply = canReply;
    }

    public Boolean getCanModerate() {
        return canModerate;
    }

    public void setCanModerate(Boolean canModerate) {
        this.canModerate = canModerate;
    }

    public String getGrantedBy() {
        return grantedBy;
    }

    public void setGrantedBy(String grantedBy) {
        this.grantedBy = grantedBy;
    }

    public LocalDateTime getGrantedAt() {
        return grantedAt;
    }

    public void setGrantedAt(LocalDateTime grantedAt) {
        this.grantedAt = grantedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
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

    public BoxType getBoxType() {
        return boxType;
    }

    public void setBoxType(BoxType boxType) {
        this.boxType = boxType;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    /**
     * 権限が有効期限内かチェック
     */
    public boolean isValid() {
        if (expiresAt == null) {
            return true;
        }
        return LocalDateTime.now().isBefore(expiresAt);
    }

    /**
     * すべての権限を付与
     */
    public void grantFullPermissions() {
        this.canView = true;
        this.canPost = true;
        this.canReply = true;
    }

    /**
     * 閲覧権限のみ付与
     */
    public void grantViewOnly() {
        this.canView = true;
        this.canPost = false;
        this.canReply = false;
        this.canModerate = false;
    }
}
