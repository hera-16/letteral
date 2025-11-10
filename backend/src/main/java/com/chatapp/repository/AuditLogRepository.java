package com.chatapp.repository;

import com.chatapp.model.AuditLog;
import com.chatapp.model.Tenant;
import com.chatapp.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 監査ログリポジトリ
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByTenantOrderByCreatedAtDesc(Tenant tenant, Pageable pageable);

    Page<AuditLog> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    Page<AuditLog> findByTenantAndActionOrderByCreatedAtDesc(Tenant tenant, String action, Pageable pageable);

    List<AuditLog> findByTenantAndEntityTypeAndEntityId(Tenant tenant, String entityType, Long entityId);

    @Query("SELECT al FROM AuditLog al WHERE al.tenant = :tenant AND al.createdAt >= :startDate AND al.createdAt <= :endDate ORDER BY al.createdAt DESC")
    List<AuditLog> findByTenantAndDateRange(@Param("tenant") Tenant tenant, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT al FROM AuditLog al WHERE al.user = :user AND al.action = :action AND al.createdAt >= :startDate ORDER BY al.createdAt DESC")
    List<AuditLog> findByUserAndActionSince(@Param("user") User user, @Param("action") String action, @Param("startDate") LocalDateTime startDate);

    Long countByTenantAndAction(Tenant tenant, String action);
}
