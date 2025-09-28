package com.chatapp.dto;

public class FriendRequestDto {
    private String targetUserId;
    
    // Constructors
    public FriendRequestDto() {}
    
    public FriendRequestDto(String targetUserId) {
        this.targetUserId = targetUserId;
    }
    
    // Getters and Setters
    public String getTargetUserId() { return targetUserId; }
    public void setTargetUserId(String targetUserId) { this.targetUserId = targetUserId; }
}