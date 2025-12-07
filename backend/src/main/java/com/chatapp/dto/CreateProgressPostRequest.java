package com.chatapp.dto;

import com.chatapp.model.enums.PostType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 進捗投稿作成リクエストDTO
 */
public class CreateProgressPostRequest {

    @NotNull(message = "テナントIDは必須です")
    private Long tenantId;

    @NotNull(message = "組織IDは必須です")
    private Long organizationId;

    @NotNull(message = "著者IDは必須です")
    private Long authorId;

    private PostType postType = PostType.PROGRESS;

    private String title;

    @NotBlank(message = "内容は必須です")
    private String content;

    private Integer achievementRate;

    private String blockers;

    private String learnings;

    private String nextAction;

    private Long targetOrganizationId;

    @NotNull(message = "投稿日は必須です")
    private String postDate;

    private List<String> tags;

    // Getters and Setters
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

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
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

    public String getPostDate() {
        return postDate;
    }

    public void setPostDate(String postDate) {
        this.postDate = postDate;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
