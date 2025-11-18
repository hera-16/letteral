package com.chatapp.repository;

import com.chatapp.model.ProgressDigest;
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
import java.util.Optional;

@Repository
public interface ProgressDigestRepository extends JpaRepository<ProgressDigest, Long> {

    Page<ProgressDigest> findByUserAndTenantOrderByPeriodStartDesc(User user, Tenant tenant, Pageable pageable);

    List<ProgressDigest> findByUserAndTenantAndDigestTypeOrderByPeriodStartDesc(
            User user, Tenant tenant, ProgressDigest.DigestType digestType);

    Optional<ProgressDigest> findByUserAndTenantAndDigestTypeAndPeriodStartAndPeriodEnd(
            User user, Tenant tenant, ProgressDigest.DigestType digestType,
            LocalDateTime periodStart, LocalDateTime periodEnd);

    @Query("SELECT pd FROM ProgressDigest pd WHERE pd.tenant = :tenant " +
           "AND pd.periodStart >= :startDate ORDER BY pd.generatedAt DESC")
    List<ProgressDigest> findRecentDigestsByTenant(
            @Param("tenant") Tenant tenant,
            @Param("startDate") LocalDateTime startDate);

    boolean existsByUserAndTenantAndDigestTypeAndPeriodStartAndPeriodEnd(
            User user, Tenant tenant, ProgressDigest.DigestType digestType,
            LocalDateTime periodStart, LocalDateTime periodEnd);
}
