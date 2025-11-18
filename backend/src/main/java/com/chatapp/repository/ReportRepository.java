package com.chatapp.repository;

import com.chatapp.model.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    Page<Report> findByTenantId(Long tenantId, Pageable pageable);

    Page<Report> findByTenantIdAndStatus(Long tenantId, Report.ReportStatus status, Pageable pageable);

    List<Report> findByTenantIdAndStatusOrderByCreatedAtDesc(Long tenantId, Report.ReportStatus status);

    @Query("SELECT r FROM Report r WHERE r.tenantId = :tenantId AND r.status = :status AND r.createdAt >= :startDate")
    List<Report> findRecentReportsByStatus(@Param("tenantId") Long tenantId,
                                           @Param("status") Report.ReportStatus status,
                                           @Param("startDate") LocalDateTime startDate);

    @Query("SELECT COUNT(r) FROM Report r WHERE r.tenantId = :tenantId AND r.status = :status")
    Long countByTenantIdAndStatus(@Param("tenantId") Long tenantId, @Param("status") Report.ReportStatus status);

    @Query("SELECT r.reportType, COUNT(r) FROM Report r WHERE r.tenantId = :tenantId GROUP BY r.reportType")
    List<Object[]> countReportsByType(@Param("tenantId") Long tenantId);
}
