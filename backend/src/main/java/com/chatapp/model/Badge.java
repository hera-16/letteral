package com.chatapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * バッジマスターエンティティ
 */
@Entity
@Table(name = "badges")
public class Badge {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false, length = 50, unique = true)
    private String badgeType;
    
    @Column(nullable = false, length = 10)
    private String icon;
    
    @Column(nullable = false)
    private Integer requirementValue = 1;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
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
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getBadgeType() {
        return badgeType;
    }
    
    public void setBadgeType(String badgeType) {
        this.badgeType = badgeType;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public void setIcon(String icon) {
        this.icon = icon;
    }
    
    public Integer getRequirementValue() {
        return requirementValue;
    }
    
    public void setRequirementValue(Integer requirementValue) {
        this.requirementValue = requirementValue;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
