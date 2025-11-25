package com.chatapp.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * レポートリクエストのDTO
 */
public class ReportRequestDTO {

    @NotNull(message = "テナントIDは必須です")
    private Long tenantId;

    @NotNull(message = "開始日は必須です")
    private LocalDate startDate;

    @NotNull(message = "終了日は必須です")
    private LocalDate endDate;

    private String reportType;

    private Long organizationId;

    private Long userId;

    // コンストラクタ
    public ReportRequestDTO() {
    }

    // Getters and Setters
    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
