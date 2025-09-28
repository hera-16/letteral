package com.chatapp.dto;

public class ChatMessageDto {
    private Long id;
    private String content;
    private String senderUsername;
    private String senderDisplayName;
    private String roomId;
    private String messageType;
    private String timestamp;
    
    // コンストラクタ
    public ChatMessageDto() {}
    
    public ChatMessageDto(String content, String senderUsername, String roomId, String messageType) {
        this.content = content;
        this.senderUsername = senderUsername;
        this.roomId = roomId;
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
    
    public String getSenderUsername() {
        return senderUsername;
    }
    
    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }
    
    public String getSenderDisplayName() {
        return senderDisplayName;
    }
    
    public void setSenderDisplayName(String senderDisplayName) {
        this.senderDisplayName = senderDisplayName;
    }
    
    public String getRoomId() {
        return roomId;
    }
    
    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
    
    public String getMessageType() {
        return messageType;
    }
    
    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
    
    public String getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}