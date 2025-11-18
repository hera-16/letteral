package com.chatapp.repository;

import com.chatapp.model.AdminActionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AdminActionLogRepository extends JpaRepository<AdminActionLog, Long> {

    Page<AdminActionLog> findByTenantIdOrderByCreatedAtDesc(Long tenantId, Pageable pageable);

    List<AdminActionLog> findByTenantIdAndAdminUserIdOrderByCreatedAtDesc(Long tenantId, Long adminUserId);

    @Query("SELECT a FROM AdminActionLog a WHERE a.tenantId = :tenantId AND a.createdAt >= :startDate ORDER BY a.createdAt DESC")
    List<AdminActionLog> findRecentLogs(@Param("tenantId") Long tenantId, @Param("startDate") LocalDateTime startDate);
}
