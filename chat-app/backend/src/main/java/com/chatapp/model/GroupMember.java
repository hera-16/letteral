package com.chatapp.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "group_members")
public class GroupMember {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;
    
    @Column(nullable = false)
    private String anonymousName; // 匿名ハンドル名
    
    @Column(nullable = false)
    private LocalDateTime joinedAt;
    
    @Column(nullable = false)
    private LocalDateTime lastAnonymousNameUpdate; // 匿名名の最終更新日時
    
    @Column(nullable = false)
    private Boolean isActive = true;
    
    // 安全性システム用フィールド
    @Column(nullable = false)
    private Integer warningCount = 0; // 警告回数
    
    @Column(nullable = false)
    private Integer reportCount = 0; // 通報回数
    
    @Column
    private LocalDateTime lastWarningDate; // 最後の警告日時
    
    @Column
    private LocalDateTime banUntil; // BAN終了日時
    
    // Constructors
    public GroupMember() {
        this.joinedAt = LocalDateTime.now();
        this.lastAnonymousNameUpdate = LocalDateTime.now();
    }
    
    public GroupMember(User user, Group group, String anonymousName) {
        this();
        this.user = user;
        this.group = group;
        this.anonymousName = anonymousName;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public Group getGroup() { return group; }
    public void setGroup(Group group) { this.group = group; }
    
    public String getAnonymousName() { return anonymousName; }
    public void setAnonymousName(String anonymousName) { this.anonymousName = anonymousName; }
    
    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
    
    public LocalDateTime getLastAnonymousNameUpdate() { return lastAnonymousNameUpdate; }
    public void setLastAnonymousNameUpdate(LocalDateTime lastAnonymousNameUpdate) { 
        this.lastAnonymousNameUpdate = lastAnonymousNameUpdate; 
    }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public Integer getWarningCount() { return warningCount; }
    public void setWarningCount(Integer warningCount) { this.warningCount = warningCount; }
    
    public Integer getReportCount() { return reportCount; }
    public void setReportCount(Integer reportCount) { this.reportCount = reportCount; }
    
    public LocalDateTime getLastWarningDate() { return lastWarningDate; }
    public void setLastWarningDate(LocalDateTime lastWarningDate) { this.lastWarningDate = lastWarningDate; }
    
    public LocalDateTime getBanUntil() { return banUntil; }
    public void setBanUntil(LocalDateTime banUntil) { this.banUntil = banUntil; }
}