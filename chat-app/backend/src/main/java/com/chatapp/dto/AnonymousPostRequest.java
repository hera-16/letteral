package com.chatapp.dto;

public class AnonymousPostRequest {
    private String content;
    
    // Constructors
    public AnonymousPostRequest() {}
    
    public AnonymousPostRequest(String content) {
        this.content = content;
    }
    
    // Getters and Setters
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}