package com.chatapp.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 投稿とOKRの紐付けエンティティ
 * 進捗投稿とOKRを関連付ける
 */
@Entity
@Table(name = "post_okr_links")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PostOkrLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private ProgressPost post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "objective_id")
    private OkrObjective objective;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "key_result_id")
    private OkrKeyResult keyResult;

    @Column(name = "contribution_note", columnDefinition = "TEXT")
    private String contributionNote;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // コンストラクタ
    public PostOkrLink() {
        this.createdAt = LocalDateTime.now();
    }

    public PostOkrLink(Tenant tenant, ProgressPost post) {
        this();
        this.tenant = tenant;
        this.post = post;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public ProgressPost getPost() {
        return post;
    }

    public void setPost(ProgressPost post) {
        this.post = post;
    }

    public OkrObjective getObjective() {
        return objective;
    }

    public void setObjective(OkrObjective objective) {
        this.objective = objective;
    }

    public OkrKeyResult getKeyResult() {
        return keyResult;
    }

    public void setKeyResult(OkrKeyResult keyResult) {
        this.keyResult = keyResult;
    }

    public String getContributionNote() {
        return contributionNote;
    }

    public void setContributionNote(String contributionNote) {
        this.contributionNote = contributionNote;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
