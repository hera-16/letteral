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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * チャレンジ達成記録エンティティ
 * ユーザーがチャレンジを達成した履歴を記録
 */
@Entity
@Table(name = "challenge_completions")
public class ChallengeCompletion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    private DailyChallenge challenge;
    
    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;
    
    @Column(name = "points_earned", nullable = false)
    private Integer pointsEarned;
    
    @Column(columnDefinition = "TEXT")
    private String note; // ユーザーのメモ（任意）
    
    @PrePersist
    protected void onCreate() {
        if (completedAt == null) {
            completedAt = LocalDateTime.now();
        }
    }
    
    // Constructors
    public ChallengeCompletion() {}
    
    public ChallengeCompletion(User user, DailyChallenge challenge, Integer pointsEarned) {
        this.user = user;
        this.challenge = challenge;
        this.pointsEarned = pointsEarned;
    }
    
    public ChallengeCompletion(User user, DailyChallenge challenge, Integer pointsEarned, String note) {
        this.user = user;
        this.challenge = challenge;
        this.pointsEarned = pointsEarned;
        this.note = note;
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
    
    public DailyChallenge getChallenge() {
        return challenge;
    }
    
    public void setChallenge(DailyChallenge challenge) {
        this.challenge = challenge;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
    
    public Integer getPointsEarned() {
        return pointsEarned;
    }
    
    public void setPointsEarned(Integer pointsEarned) {
        this.pointsEarned = pointsEarned;
    }
    
    public String getNote() {
        return note;
    }
    
    public void setNote(String note) {
        this.note = note;
    }
}
