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

@Entity
@Table(name = "`friends`")
public class Friend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "addressee_id", nullable = false)
    private User addressee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FriendStatus status;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    public enum FriendStatus {
        PENDING,
        ACCEPTED,
        REJECTED,
        BLOCKED
    }

    public Friend() {
        this.requestedAt = LocalDateTime.now();
        this.status = FriendStatus.PENDING;
    }

    public Friend(final User requester, final User addressee) {
        this();
        this.requester = requester;
        this.addressee = addressee;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public User getRequester() {
        return requester;
    }

    public void setRequester(final User requester) {
        this.requester = requester;
    }

    public User getAddressee() {
        return addressee;
    }

    public void setAddressee(final User addressee) {
        this.addressee = addressee;
    }

    public FriendStatus getStatus() {
        return status;
    }

    public void setStatus(final FriendStatus status) {
        this.status = status;
        if (status != FriendStatus.PENDING) {
            this.respondedAt = LocalDateTime.now();
        }
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(final LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public LocalDateTime getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(final LocalDateTime respondedAt) {
        this.respondedAt = respondedAt;
    }
}
