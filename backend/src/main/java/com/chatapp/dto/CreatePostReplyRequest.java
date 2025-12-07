package com.chatapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 返信作成リクエスト
 */
public class CreatePostReplyRequest {

    @NotNull(message = "投稿IDは必須です")
    private Long postId;

    @NotBlank(message = "返信内容は必須です")
    private String content;

    // Constructors
    public CreatePostReplyRequest() {}

    public CreatePostReplyRequest(Long postId, String content) {
        this.postId = postId;
        this.content = content;
    }

    // Getters and Setters
    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
