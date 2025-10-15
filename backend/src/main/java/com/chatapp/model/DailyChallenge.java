package com.chatapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * デイリーチャレンジエンティティ
 * ユーザーが毎日取り組める小さな課題を管理
 */
@Entity
@Table(name = "daily_challenges")
public class DailyChallenge {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String title;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private Integer points = 10;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "challenge_type", nullable = false, length = 50)
    private ChallengeType challengeType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level", nullable = false, length = 20)
    private DifficultyLevel difficultyLevel = DifficultyLevel.EASY;
    
    @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean isActive = true;
    
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Constructors
    public DailyChallenge() {}
    
    public DailyChallenge(String title, String description, Integer points, 
                          ChallengeType challengeType, DifficultyLevel difficultyLevel) {
        this.title = title;
        this.description = description;
        this.points = points;
        this.challengeType = challengeType;
        this.difficultyLevel = difficultyLevel;
    }
    
    // Enums
    public enum ChallengeType {
        GRATITUDE,    // 感謝
        KINDNESS,     // 優しさ
        SELF_CARE,    // セルフケア
        CREATIVITY,   // 創造性
        CONNECTION    // つながり
    }
    
    public enum DifficultyLevel {
        EASY,
        MEDIUM,
        HARD
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Integer getPoints() {
        return points;
    }
    
    public void setPoints(Integer points) {
        this.points = points;
    }
    
    public ChallengeType getChallengeType() {
        return challengeType;
    }
    
    public void setChallengeType(ChallengeType challengeType) {
        this.challengeType = challengeType;
    }
    
    public DifficultyLevel getDifficultyLevel() {
        return difficultyLevel;
    }
    
    public void setDifficultyLevel(DifficultyLevel difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
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
}
