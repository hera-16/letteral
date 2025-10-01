package com.chatapp.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Represents a group that can be either invite-only or public topic-based.
 */
@Entity
@Table(name = "groups")
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 1, max = 100)
    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "group_type", nullable = false)
    private GroupType groupType;

    @Column(name = "invite_code", unique = true)
    private String inviteCode;

    @Column(name = "max_members")
    private Integer maxMembers;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<GroupMember> members = new HashSet<>();

    /**
     * Type of group.
     */
    public enum GroupType {
        INVITE_ONLY,
        PUBLIC_TOPIC
    }

    public Group() {
        this.createdAt = LocalDateTime.now();
    }

    public Group(final String name, final String description,
            final GroupType groupType, final User creator) {
        this();
        this.name = name;
        this.description = description;
        this.groupType = groupType;
        this.creator = creator;

        if (groupType == GroupType.INVITE_ONLY) {
            this.inviteCode = generateInviteCode();
        }
    }

    private String generateInviteCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public void regenerateInviteCode() {
        if (this.groupType == GroupType.INVITE_ONLY) {
            this.inviteCode = generateInviteCode();
        }
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public GroupType getGroupType() {
        return groupType;
    }

    public void setGroupType(final GroupType groupType) {
        this.groupType = groupType;
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(final String inviteCode) {
        this.inviteCode = inviteCode;
    }

    public Integer getMaxMembers() {
        return maxMembers;
    }

    public void setMaxMembers(final Integer maxMembers) {
        this.maxMembers = maxMembers;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(final User creator) {
        this.creator = creator;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(final LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Set<GroupMember> getMembers() {
        return members;
    }

    public void setMembers(final Set<GroupMember> members) {
        this.members = members;
    }

    public void addMember(final GroupMember member) {
        this.members.add(member);
        member.setGroup(this);
    }

    public void removeMember(final GroupMember member) {
        this.members.remove(member);
        member.setGroup(null);
    }
}
