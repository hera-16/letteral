package com.chatapp.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group; // Letteralのグループ機能対応
    
    @Column(name = "room_id")
    private String roomId;
    
    @Column(name = "anonymous_sender_name")
    private String anonymousSenderName; // 匿名投稿時の表示名
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "scheduled_delete_at")
    private LocalDateTime scheduledDeleteAt; // 自動削除予定時刻 (毎朝7:00)
    
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type")
    private MessageType messageType;
    
    // 安全性システム用フィールド
    @Column(name = "report_count", nullable = false)
    private Integer reportCount = 0; // 通報回数
    
    @Column(name = "hide_count", nullable = false)
    private Integer hideCount = 0; // 非表示回数
    
    @Column(name = "is_hidden", nullable = false)
    private Boolean isHidden = false; // 非表示状態
    
    @Column(name = "is_reported", nullable = false)
    private Boolean isReported = false; // 通報状態
    
    public enum MessageType {
        CHAT, JOIN, LEAVE, ANONYMOUS_POST, ANONYMOUS_COMMENT
    }
    
    // コンストラクタ
    public ChatMessage() {
        this.createdAt = LocalDateTime.now();
        this.messageType = MessageType.CHAT;
        // デフォルトで翌朝7:00に削除予定
        this.scheduledDeleteAt = LocalDateTime.now().plusDays(1).withHour(7).withMinute(0).withSecond(0);
    }
    
    public ChatMessage(String content, User sender, String roomId) {
        this();
        this.content = content;
        this.sender = sender;
        this.roomId = roomId;
    }
    
    public ChatMessage(String content, User sender, String roomId, MessageType messageType) {
        this(content, sender, roomId);
        this.messageType = messageType;
    }
    
    // Letteral用のコンストラクタ（匿名投稿）
    public ChatMessage(String content, User sender, Group group, String anonymousSenderName, MessageType messageType) {
        this();
        this.content = content;
        this.sender = sender;
        this.group = group;
        this.roomId = group != null ? "group_" + group.getId() : "general";
        this.anonymousSenderName = anonymousSenderName;
        this.messageType = messageType;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public User getSender() {
        return sender;
    }
    
    public void setSender(User sender) {
        this.sender = sender;
    }
    
    public String getRoomId() {
        return roomId;
    }
    
    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public MessageType getMessageType() {
        return messageType;
    }
    
    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }
    
    // Letteral用の新しいフィールドのgetters and setters
    public Group getGroup() {
        return group;
    }
    
    public void setGroup(Group group) {
        this.group = group;
    }
    
    public String getAnonymousSenderName() {
        return anonymousSenderName;
    }
    
    public void setAnonymousSenderName(String anonymousSenderName) {
        this.anonymousSenderName = anonymousSenderName;
    }
    
    public LocalDateTime getScheduledDeleteAt() {
        return scheduledDeleteAt;
    }
    
    public void setScheduledDeleteAt(LocalDateTime scheduledDeleteAt) {
        this.scheduledDeleteAt = scheduledDeleteAt;
    }
    
    public Integer getReportCount() {
        return reportCount;
    }
    
    public void setReportCount(Integer reportCount) {
        this.reportCount = reportCount;
    }
    
    public Integer getHideCount() {
        return hideCount;
    }
    
    public void setHideCount(Integer hideCount) {
        this.hideCount = hideCount;
    }
    
    public Boolean getIsHidden() {
        return isHidden;
    }
    
    public void setIsHidden(Boolean isHidden) {
        this.isHidden = isHidden;
    }
    
    public Boolean getIsReported() {
        return isReported;
    }
    
    public void setIsReported(Boolean isReported) {
        this.isReported = isReported;
    }
}