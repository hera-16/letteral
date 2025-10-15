package com.chatapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 共有投稿へのリアクションエンティティ
 */
@Entity
@Table(name = "challenge_share_reactions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"share_id", "user_id"}))
public class ChallengeShareReaction {

    public enum ReactionType {
        ENCOURAGE,
        EMPATHY,
        AWESOME
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "share_id", nullable = false)
    private ChallengeShare share;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private ReactionType type;

    @Column(name = "reacted_at", nullable = false)
    private LocalDateTime reactedAt;

    @PrePersist
    protected void onCreate() {
        if (reactedAt == null) {
            reactedAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ChallengeShare getShare() {
        return share;
    }

    public void setShare(ChallengeShare share) {
        this.share = share;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ReactionType getType() {
        return type;
    }

    public void setType(ReactionType type) {
        this.type = type;
    }

    public LocalDateTime getReactedAt() {
        return reactedAt;
    }

    public void setReactedAt(LocalDateTime reactedAt) {
        this.reactedAt = reactedAt;
    }
}
