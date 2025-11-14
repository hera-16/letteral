package com.chatapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * テナント情報のDTO
 */
public class TenantDTO {

    private Long id;

    @NotBlank(message = "企業名は必須です")
    @Size(max = 200, message = "企業名は200文字以内で入力してください")
    private String name;

    @NotBlank(message = "スラッグは必須です")
    @Size(max = 100, message = "スラッグは100文字以内で入力してください")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "スラッグは小文字英数字とハイフンのみ使用できます")
    private String slug;

    @NotBlank(message = "プランタイプは必須です")
    @Pattern(regexp = "^(FREE|PRO|ENTERPRISE)$", message = "プランタイプはFREE、PRO、ENTERPRISEのいずれかです")
    private String planType;

    @Positive(message = "最大ユーザー数は正の数で入力してください")
    private Integer maxUsers;

    @Positive(message = "最大ストレージ容量は正の数で入力してください")
    private Integer maxStorageGb;

    @Pattern(regexp = "^(ACTIVE|SUSPENDED|CLOSED)$", message = "ステータスはACTIVE、SUSPENDED、CLOSEDのいずれかです")
    private String status;

    @Email(message = "有効なメールアドレスを入力してください")
    private String contactEmail;

    private String settings;

    // コンストラクタ
    public TenantDTO() {
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

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getPlanType() {
        return planType;
    }

    public void setPlanType(String planType) {
        this.planType = planType;
    }

    public Integer getMaxUsers() {
        return maxUsers;
    }

    public void setMaxUsers(Integer maxUsers) {
        this.maxUsers = maxUsers;
    }

    public Integer getMaxStorageGb() {
        return maxStorageGb;
    }

    public void setMaxStorageGb(Integer maxStorageGb) {
        this.maxStorageGb = maxStorageGb;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getSettings() {
        return settings;
    }

    public void setSettings(String settings) {
        this.settings = settings;
    }
}
