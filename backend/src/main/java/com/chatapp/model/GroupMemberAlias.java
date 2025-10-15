package com.chatapp.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * グループ内での匿名名マッピング
 * target_user_id を group_id 内で見たときの匿名名を保持
 * 全員が同じ匿名名を見る(viewer_idは不要)
 */
@Entity
@Table(name = "group_member_aliases", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"target_user_id", "group_id"}))
public class GroupMemberAlias {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_user_id", nullable = false)
    private User targetUser;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;
    
    @Column(name = "anonymous_name", nullable = false, length = 100)
    private String anonymousName;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_rotation_date", nullable = false)
    private LocalDate lastRotationDate;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (lastRotationDate == null) {
            lastRotationDate = LocalDate.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public GroupMemberAlias() {}
    
    public GroupMemberAlias(User targetUser, Group group, String anonymousName) {
        this.targetUser = targetUser;
        this.group = group;
        this.anonymousName = anonymousName;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getTargetUser() {
        return targetUser;
    }
    
    public void setTargetUser(User targetUser) {
        this.targetUser = targetUser;
    }
    
    public Group getGroup() {
        return group;
    }
    
    public void setGroup(Group group) {
        this.group = group;
    }
    
    public String getAnonymousName() {
        return anonymousName;
    }
    
    public void setAnonymousName(String anonymousName) {
        this.anonymousName = anonymousName;
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

    public LocalDate getLastRotationDate() {
        return lastRotationDate;
    }

    public void setLastRotationDate(LocalDate lastRotationDate) {
        this.lastRotationDate = lastRotationDate;
    }
}