package com.chatapp.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ユーザー進捗エンティティ
 * ユーザーのチャレンジ達成状況と花の成長レベルを管理
 */
@Entity
@Table(name = "user_progress")
public class UserProgress {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @Column(name = "total_points", nullable = false)
    private Integer totalPoints = 0;
    
    @Column(name = "flower_level", nullable = false)
    private Integer flowerLevel = 1;
    
    @Column(name = "current_streak", nullable = false)
    private Integer currentStreak = 0;
    
    @Column(name = "longest_streak", nullable = false)
    private Integer longestStreak = 0;
    
    @Column(name = "last_challenge_date")
    private LocalDate lastChallengeDate;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
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
    
    // Constructors
    public UserProgress() {}
    
    public UserProgress(User user) {
        this.user = user;
    }
    
    /**
     * ポイントを追加して花レベルを更新
     */
    public void addPoints(Integer points) {
        this.totalPoints += points;
        updateFlowerLevel();
    }
    
    /**
     * 連続達成日数を更新
     */
    public void updateStreak(LocalDate today) {
        if (lastChallengeDate == null) {
            // 初回
            currentStreak = 1;
        } else if (lastChallengeDate.plusDays(1).equals(today)) {
            // 連続達成
            currentStreak++;
            if (currentStreak > longestStreak) {
                longestStreak = currentStreak;
            }
        } else if (lastChallengeDate.equals(today)) {
            // 同日（何もしない）
        } else {
            // 途切れた
            currentStreak = 1;
        }
        lastChallengeDate = today;
    }
    
    /**
     * 花レベルを計算（100ポイントで1レベルアップ、最大10レベル）
     */
    private void updateFlowerLevel() {
        int newLevel = Math.min((totalPoints / 100) + 1, 10);
        this.flowerLevel = newLevel;
    }
    
    /**
     * 花の絵文字を取得
     */
    public String getFlowerEmoji() {
        return switch (flowerLevel) {
            case 1 -> "🌱"; // 芽
            case 2 -> "🌿"; // 若葉
            case 3 -> "🍀"; // クローバー
            case 4 -> "🌾"; // 穂
            case 5 -> "🌺"; // ハイビスカス
            case 6 -> "🌻"; // ひまわり
            case 7 -> "🌷"; // チューリップ
            case 8 -> "🌹"; // バラ
            case 9 -> "🌸"; // 桜
            case 10 -> "🏵️"; // 満開
            default -> "🌱";
        };
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
    
    public Integer getTotalPoints() {
        return totalPoints;
    }
    
    public void setTotalPoints(Integer totalPoints) {
        this.totalPoints = totalPoints;
    }
    
    public Integer getFlowerLevel() {
        return flowerLevel;
    }
    
    public void setFlowerLevel(Integer flowerLevel) {
        this.flowerLevel = flowerLevel;
    }
    
    public Integer getCurrentStreak() {
        return currentStreak;
    }
    
    public void setCurrentStreak(Integer currentStreak) {
        this.currentStreak = currentStreak;
    }
    
    public Integer getLongestStreak() {
        return longestStreak;
    }
    
    public void setLongestStreak(Integer longestStreak) {
        this.longestStreak = longestStreak;
    }
    
    public LocalDate getLastChallengeDate() {
        return lastChallengeDate;
    }
    
    public void setLastChallengeDate(LocalDate lastChallengeDate) {
        this.lastChallengeDate = lastChallengeDate;
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
}
