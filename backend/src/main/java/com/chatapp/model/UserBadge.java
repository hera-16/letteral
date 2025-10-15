package com.chatapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * ユーザーバッジ獲得記録エンティティ
 */
@Entity
@Table(name = "user_badges")
public class UserBadge {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "badge_id", nullable = false)
    private Badge badge;
    
    @Column(nullable = false)
    private LocalDateTime earnedAt;
    
    @Column(nullable = false)
    private Boolean isNew = true;
    
    @PrePersist
    protected void onCreate() {
        earnedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public Badge getBadge() {
        return badge;
    }
    
    public void setBadge(Badge badge) {
        this.badge = badge;
    }
    
    public LocalDateTime getEarnedAt() {
        return earnedAt;
    }
    
    public void setEarnedAt(LocalDateTime earnedAt) {
        this.earnedAt = earnedAt;
    }
    
    public Boolean getIsNew() {
        return isNew;
    }
    
    public void setIsNew(Boolean isNew) {
        this.isNew = isNew;
    }
}
