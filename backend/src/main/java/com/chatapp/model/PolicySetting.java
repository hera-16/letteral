package com.chatapp.model;

import com.chatapp.model.enums.AnonymityLevel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * ポリシー設定エンティティ
 * 組織ごとの匿名度・公開範囲・モデレーションポリシー
 */
@Entity
@Table(name = "policy_settings")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PolicySetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_anonymity_level", nullable = false, length = 20)
    private AnonymityLevel defaultAnonymityLevel = AnonymityLevel.PSEUDONYM;

    @Column(name = "allow_full_anonymous", nullable = false)
    private Boolean allowFullAnonymous = true;

    @Column(name = "allow_real_name", nullable = false)
    private Boolean allowRealName = true;

    @Column(name = "allow_company_wide", nullable = false)
    private Boolean allowCompanyWide = false;

    @Column(name = "require_approval", nullable = false)
    private Boolean requireApproval = false;

    @Column(name = "min_post_length", nullable = false)
    private Integer minPostLength = 10;

    @Column(name = "max_post_length", nullable = false)
    private Integer maxPostLength = 5000;

    @Column(name = "enable_ng_word_filter", nullable = false)
    private Boolean enableNgWordFilter = true;

    @Column(name = "enable_auto_moderation", nullable = false)
    private Boolean enableAutoModeration = true;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ng_words", columnDefinition = "JSON")
    private List<String> ngWords;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSON")
    private Map<String, Object> settings;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // コンストラクタ
    public PolicySetting() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public PolicySetting(Tenant tenant, Organization organization) {
        this();
        this.tenant = tenant;
        this.organization = organization;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public AnonymityLevel getDefaultAnonymityLevel() {
        return defaultAnonymityLevel;
    }

    public void setDefaultAnonymityLevel(AnonymityLevel defaultAnonymityLevel) {
        this.defaultAnonymityLevel = defaultAnonymityLevel;
    }

    public Boolean getAllowFullAnonymous() {
        return allowFullAnonymous;
    }

    public void setAllowFullAnonymous(Boolean allowFullAnonymous) {
        this.allowFullAnonymous = allowFullAnonymous;
    }

    public Boolean getAllowRealName() {
        return allowRealName;
    }

    public void setAllowRealName(Boolean allowRealName) {
        this.allowRealName = allowRealName;
    }

    public Boolean getAllowCompanyWide() {
        return allowCompanyWide;
    }

    public void setAllowCompanyWide(Boolean allowCompanyWide) {
        this.allowCompanyWide = allowCompanyWide;
    }

    public Boolean getRequireApproval() {
        return requireApproval;
    }

    public void setRequireApproval(Boolean requireApproval) {
        this.requireApproval = requireApproval;
    }

    public Integer getMinPostLength() {
        return minPostLength;
    }

    public void setMinPostLength(Integer minPostLength) {
        this.minPostLength = minPostLength;
    }

    public Integer getMaxPostLength() {
        return maxPostLength;
    }

    public void setMaxPostLength(Integer maxPostLength) {
        this.maxPostLength = maxPostLength;
    }

    public Boolean getEnableNgWordFilter() {
        return enableNgWordFilter;
    }

    public void setEnableNgWordFilter(Boolean enableNgWordFilter) {
        this.enableNgWordFilter = enableNgWordFilter;
    }

    public Boolean getEnableAutoModeration() {
        return enableAutoModeration;
    }

    public void setEnableAutoModeration(Boolean enableAutoModeration) {
        this.enableAutoModeration = enableAutoModeration;
    }

    public List<String> getNgWords() {
        return ngWords;
    }

    public void setNgWords(List<String> ngWords) {
        this.ngWords = ngWords;
    }

    public Map<String, Object> getSettings() {
        return settings;
    }

    public void setSettings(Map<String, Object> settings) {
        this.settings = settings;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
