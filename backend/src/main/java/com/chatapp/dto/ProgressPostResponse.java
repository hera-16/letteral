package com.chatapp.dto;

import com.chatapp.model.ProgressPost;
import com.chatapp.model.enums.PostType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 進捗投稿のレスポンスDTO
 * 著者の所属組織情報を含む
 */
public class ProgressPostResponse {

    private Long id;
    private Long tenantId;
    private Long organizationId;
    private String organizationName;
    private Long authorId;
    private String authorUsername;
    private String authorDisplayName;
    private Integer anonymousNumber; // 匿名番号（組織内での連番）
    private String formattedAnonymousNumber; // フォーマット済み匿名番号（例: #0001）
    private List<OrganizationInfo> authorOrganizations; // 著者が所属している組織のリスト
    private PostType postType;
    private String title;
    private String content;
    private Integer achievementRate;
    private String blockers;
    private String learnings;
    private String nextAction;
    private Long targetOrganizationId;
    private LocalDate postDate;
    private List<String> tags;
    private List<Map<String, Object>> attachments;
    private Integer commentCount;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private BoxTypeInfo boxType;

    // 内部クラス: Box Type情報
    public static class BoxTypeInfo {
        private Long id;
        private String name;
        private String displayName;

        public BoxTypeInfo() {
        }

        public BoxTypeInfo(Long id, String name, String displayName) {
            this.id = id;
            this.name = name;
            this.displayName = displayName;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }
    }

    // 内部クラス: 組織情報
    public static class OrganizationInfo {
        private Long id;
        private String name;
        private String organizationType;
        private Integer level;
        private String role; // ユーザーのその組織での役割

        public OrganizationInfo() {
        }

        public OrganizationInfo(Long id, String name, String organizationType, Integer level, String role) {
            this.id = id;
            this.name = name;
            this.organizationType = organizationType;
            this.level = level;
            this.role = role;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getOrganizationType() {
            return organizationType;
        }

        public void setOrganizationType(String organizationType) {
            this.organizationType = organizationType;
        }

        public Integer getLevel() {
            return level;
        }

        public void setLevel(Integer level) {
            this.level = level;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }

    // コンストラクタ
    public ProgressPostResponse() {
    }

    public ProgressPostResponse(ProgressPost post, List<OrganizationInfo> authorOrganizations) {
        this.id = post.getId();
        this.tenantId = post.getTenant() != null ? post.getTenant().getId() : null;
        this.organizationId = post.getOrganization() != null ? post.getOrganization().getId() : null;
        this.organizationName = post.getOrganization() != null ? post.getOrganization().getName() : null;
        this.authorId = post.getAuthor() != null ? post.getAuthor().getId() : null;
        this.authorUsername = post.getAuthor() != null ? post.getAuthor().getUsername() : null;
        this.authorDisplayName = post.getAuthor() != null ? post.getAuthor().getDisplayName() : null;
        this.anonymousNumber = post.getAnonymousNumber();
        this.formattedAnonymousNumber = post.getFormattedAnonymousNumber();
        this.authorOrganizations = authorOrganizations;
        this.postType = post.getPostType();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.achievementRate = post.getAchievementRate();
        this.blockers = post.getBlockers();
        this.learnings = post.getLearnings();
        this.nextAction = post.getNextAction();
        this.targetOrganizationId = post.getTargetOrganization() != null ? post.getTargetOrganization().getId() : null;
        this.postDate = post.getPostDate();
        this.tags = post.getTags();
        this.attachments = post.getAttachments();
        this.commentCount = post.getCommentCount();
        this.viewCount = post.getViewCount();
        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();

        // BoxType情報の設定
        if (post.getBoxType() != null) {
            this.boxType = new BoxTypeInfo(
                post.getBoxType().getId(),
                post.getBoxType().getBoxName(),
                post.getBoxType().getDisplayName()
            );
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public void setAuthorUsername(String authorUsername) {
        this.authorUsername = authorUsername;
    }

    public String getAuthorDisplayName() {
        return authorDisplayName;
    }

    public void setAuthorDisplayName(String authorDisplayName) {
        this.authorDisplayName = authorDisplayName;
    }

    public Integer getAnonymousNumber() {
        return anonymousNumber;
    }

    public void setAnonymousNumber(Integer anonymousNumber) {
        this.anonymousNumber = anonymousNumber;
    }

    public String getFormattedAnonymousNumber() {
        return formattedAnonymousNumber;
    }

    public void setFormattedAnonymousNumber(String formattedAnonymousNumber) {
        this.formattedAnonymousNumber = formattedAnonymousNumber;
    }

    public List<OrganizationInfo> getAuthorOrganizations() {
        return authorOrganizations;
    }

    public void setAuthorOrganizations(List<OrganizationInfo> authorOrganizations) {
        this.authorOrganizations = authorOrganizations;
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

    public Long getTargetOrganizationId() {
        return targetOrganizationId;
    }

    public void setTargetOrganizationId(Long targetOrganizationId) {
        this.targetOrganizationId = targetOrganizationId;
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

    public BoxTypeInfo getBoxType() {
        return boxType;
    }

    public void setBoxType(BoxTypeInfo boxType) {
        this.boxType = boxType;
    }
}
