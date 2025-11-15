package com.chatapp.model;

import com.chatapp.model.enums.PostType;
import com.chatapp.model.enums.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 進捗投稿エンティティ
 * 日々の目標・進捗・課題を記録
 */
@Entity
@Table(name = "progress_posts")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ProgressPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "box_type_id")
    private BoxType boxType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_post_id")
    private ProgressPost parentPost;

    @Column(name = "is_reply", nullable = false)
    private Boolean isReply = false;

    @Column(name = "reply_depth", nullable = false)
    private Integer replyDepth = 0;

    @Column(name = "reply_count", nullable = false)
    private Integer replyCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Enumerated(EnumType.STRING)
    @Column(name = "post_type", nullable = false, length = 20)
    private PostType postType = PostType.PROGRESS;

    @Size(max = 200)
    @Column(length = 200)
    private String title;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "achievement_rate")
    private Integer achievementRate;

    @Column(columnDefinition = "TEXT")
    private String blockers;

    @Column(columnDefinition = "TEXT")
    private String learnings;

    @Column(name = "next_action", columnDefinition = "TEXT")
    private String nextAction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Visibility visibility = Visibility.ORGANIZATION;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_organization_id")
    private Organization targetOrganization;

    @Column(name = "post_date", nullable = false)
    private LocalDate postDate;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSON")
    private List<String> tags;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSON")
    private List<Map<String, Object>> attachments;

    @Column(name = "reaction_count", nullable = false)
    private Integer reactionCount = 0;

    @Column(name = "comment_count", nullable = false)
    private Integer commentCount = 0;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // コンストラクタ
    public ProgressPost() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public ProgressPost(Tenant tenant, Organization organization, User author, String content, LocalDate postDate) {
        this();
        this.tenant = tenant;
        this.organization = organization;
        this.author = author;
        this.content = content;
        this.postDate = postDate;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
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

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public PostType getPostType() {
        return postType;
    }

    public void setPostType(PostType postType) {
        this.postType = postType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getAchievementRate() {
        return achievementRate;
    }

    public void setAchievementRate(Integer achievementRate) {
        this.achievementRate = achievementRate;
    }

    public String getBlockers() {
        return blockers;
    }

    public void setBlockers(String blockers) {
        this.blockers = blockers;
    }

    public String getLearnings() {
        return learnings;
    }

    public void setLearnings(String learnings) {
        this.learnings = learnings;
    }

    public String getNextAction() {
        return nextAction;
    }

    public void setNextAction(String nextAction) {
        this.nextAction = nextAction;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public Organization getTargetOrganization() {
        return targetOrganization;
    }

    public void setTargetOrganization(Organization targetOrganization) {
        this.targetOrganization = targetOrganization;
    }

    public LocalDate getPostDate() {
        return postDate;
    }

    public void setPostDate(LocalDate postDate) {
        this.postDate = postDate;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<Map<String, Object>> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Map<String, Object>> attachments) {
        this.attachments = attachments;
    }

    public Integer getReactionCount() {
        return reactionCount;
    }

    public void setReactionCount(Integer reactionCount) {
        this.reactionCount = reactionCount;
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
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

    public BoxType getBoxType() {
        return boxType;
    }

    public void setBoxType(BoxType boxType) {
        this.boxType = boxType;
    }

    public ProgressPost getParentPost() {
        return parentPost;
    }

    public void setParentPost(ProgressPost parentPost) {
        this.parentPost = parentPost;
    }

    public Boolean getIsReply() {
        return isReply;
    }

    public void setIsReply(Boolean isReply) {
        this.isReply = isReply;
    }

    public Integer getReplyDepth() {
        return replyDepth;
    }

    public void setReplyDepth(Integer replyDepth) {
        this.replyDepth = replyDepth;
    }

    public Integer getReplyCount() {
        return replyCount;
    }

    public void setReplyCount(Integer replyCount) {
        this.replyCount = replyCount;
    }

    /**
     * 返信を追加
     */
    public void incrementReplyCount() {
        this.replyCount++;
    }

    /**
     * 返信かどうかをチェック
     */
    public boolean isReplyPost() {
        return Boolean.TRUE.equals(this.isReply);
    }

    /**
     * ルート投稿かどうかをチェック
     */
    public boolean isRootPost() {
        return !isReplyPost();
    }
}
