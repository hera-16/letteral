package com.chatapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * チャレンジ達成共有エンティティ
 */
@Entity
@Table(name = "challenge_shares")
public class ChallengeShare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "challenge_id", nullable = false)
    private DailyChallenge challenge;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String comment;

    @Column(length = 50)
    private String mood;

    @Column(name = "shared_at", nullable = false)
    private LocalDateTime sharedAt;

    @PrePersist
    protected void onCreate() {
        if (sharedAt == null) {
            sharedAt = LocalDateTime.now();
        }
    }

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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public LocalDateTime getSharedAt() {
        return sharedAt;
    }

    public void setSharedAt(LocalDateTime sharedAt) {
        this.sharedAt = sharedAt;
    }
}
