package com.chatapp.dto;

import com.chatapp.model.Organization;
import java.util.ArrayList;
import java.util.List;

/**
 * 組織ツリーのノードDTO
 * 階層構造を表現するための再帰的データ構造
 */
public class OrganizationTreeNode {
    private Long id;
    private String name;
    private String description;
    private String organizationType;
    private Integer level;
    private String path;
    private Integer memberCount;
    private List<OrganizationTreeNode> children;

    public OrganizationTreeNode() {
        this.children = new ArrayList<>();
    }

    public OrganizationTreeNode(Organization organization) {
        this.id = organization.getId();
        this.name = organization.getName();
        this.description = organization.getDescription();
        this.organizationType = organization.getOrganizationType();
        this.level = organization.getLevel();
        this.path = organization.getPath();
        this.memberCount = 0; // 後で設定
        this.children = new ArrayList<>();
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

    public String getOrganizationType() {
        return organizationType;
    }

    public void setOrganizationType(String organizationType) {
        this.organizationType = organizationType;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Integer getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(Integer memberCount) {
        this.memberCount = memberCount;
    }

    public List<OrganizationTreeNode> getChildren() {
        return children;
    }

    public void setChildren(List<OrganizationTreeNode> children) {
        this.children = children;
    }

    public void addChild(OrganizationTreeNode child) {
        this.children.add(child);
    }
}
