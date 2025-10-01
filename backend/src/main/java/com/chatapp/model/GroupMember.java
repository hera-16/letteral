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
import jakarta.persistence.UniqueConstraint;

/**
 * Represents a user's membership in a group with a specific role.
 */
@Entity
@Table(name = "group_members", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "group_id", "user_id" })
})
public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private MemberRole role;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    /**
     * Role of a member within a group.
     */
    public enum MemberRole {
        ADMIN,
        MEMBER
    }

    public GroupMember() {
        this.joinedAt = LocalDateTime.now();
        this.role = MemberRole.MEMBER;
    }

    public GroupMember(final Group group, final User user, final MemberRole role) {
        this();
        this.group = group;
        this.user = user;
        this.role = role;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(final Group group) {
        this.group = group;
    }

    public User getUser() {
        return user;
    }

    public void setUser(final User user) {
        this.user = user;
    }

    public MemberRole getRole() {
        return role;
    }

    public void setRole(final MemberRole role) {
        this.role = role;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(final LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }
}
