package com.chatapp.dto;

public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String username;
    private String email;
    private String displayName;
    
    public JwtResponse(String accessToken, Long id, String username, String email, String displayName) {
        this.token = accessToken;
        this.id = id;
        this.username = username;
        this.email = email;
        this.displayName = displayName;
    }
    
    // Getters and Setters
    public String getAccessToken() {
        return token;
    }
    
    public void setAccessToken(String accessToken) {
        this.token = accessToken;
    }
    
    public String getTokenType() {
        return type;
    }
    
    public void setTokenType(String tokenType) {
        this.type = tokenType;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}