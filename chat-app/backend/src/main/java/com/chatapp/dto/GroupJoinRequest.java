package com.chatapp.dto;

public class GroupJoinRequest {
    private String inviteCode;
    
    // Constructors
    public GroupJoinRequest() {}
    
    public GroupJoinRequest(String inviteCode) {
        this.inviteCode = inviteCode;
    }
    
    // Getters and Setters
    public String getInviteCode() { return inviteCode; }
    public void setInviteCode(String inviteCode) { this.inviteCode = inviteCode; }
}