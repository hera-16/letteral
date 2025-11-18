package com.chatapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "progress_digests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProgressDigest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DigestType digestType; // WEEKLY, MONTHLY

    @Column(nullable = false)
    private LocalDateTime periodStart;

    @Column(nullable = false)
    private LocalDateTime periodEnd;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(nullable = false)
    private Integer totalPosts;

    @Column(nullable = false)
    private Integer completedGoals;

    @Column(nullable = false)
    private Integer ongoingGoals;

    @Column(nullable = false)
    private Integer challenges;

    @Column(columnDefinition = "TEXT")
    private String topAchievements;

    @Column(columnDefinition = "TEXT")
    private String topChallenges;

    @Column(columnDefinition = "TEXT")
    private String keyLearnings;

    @Column(columnDefinition = "TEXT")
    private String nextSteps;

    @Column(nullable = false)
    private LocalDateTime generatedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        generatedAt = LocalDateTime.now();
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum DigestType {
        WEEKLY,
        MONTHLY,
        QUARTERLY
    }
}
