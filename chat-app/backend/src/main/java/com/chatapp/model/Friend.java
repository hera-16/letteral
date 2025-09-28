package com.chatapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "friends")
public class Friend {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester; // フレンド申請を送った人
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "addressee_id", nullable = false)
    private User addressee; // フレンド申請を受けた人
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FriendStatus status;
    
    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;
    
    @Column(name = "responded_at")
    private LocalDateTime respondedAt;
    
    public enum FriendStatus {
        PENDING,   // 申請中
        ACCEPTED,  // 承認済み
        REJECTED,  // 拒否
        BLOCKED    // ブロック
    }
    
    // Constructors
    public Friend() {
        this.requestedAt = LocalDateTime.now();
        this.status = FriendStatus.PENDING;
    }
    
    public Friend(User requester, User addressee) {
        this();
        this.requester = requester;
        this.addressee = addressee;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getRequester() { return requester; }
    public void setRequester(User requester) { this.requester = requester; }
    
    public User getAddressee() { return addressee; }
    public void setAddressee(User addressee) { this.addressee = addressee; }
    
    public FriendStatus getStatus() { return status; }
    public void setStatus(FriendStatus status) { 
        this.status = status;
        if (status != FriendStatus.PENDING) {
            this.respondedAt = LocalDateTime.now();
        }
    }
    
    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }
    
    public LocalDateTime getRespondedAt() { return respondedAt; }
    public void setRespondedAt(LocalDateTime respondedAt) { this.respondedAt = respondedAt; }
}
