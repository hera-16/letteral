package com.chatapp.repository;

import com.chatapp.model.EvaluationSnapshot;
import com.chatapp.model.Organization;
import com.chatapp.model.Tenant;
import com.chatapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 評価スナップショットリポジトリ
 */
@Repository
public interface EvaluationSnapshotRepository extends JpaRepository<EvaluationSnapshot, Long> {

    List<EvaluationSnapshot> findByUserOrderByPeriodStartDesc(User user);

    List<EvaluationSnapshot> findByTenantAndUserOrderByPeriodStartDesc(Tenant tenant, User user);

    Optional<EvaluationSnapshot> findByUserAndOrganizationAndPeriodStartAndPeriodEnd(User user, Organization organization, LocalDate periodStart, LocalDate periodEnd);

    List<EvaluationSnapshot> findByOrganizationAndPeriodStartAndPeriodEnd(Organization organization, LocalDate periodStart, LocalDate periodEnd);

    @Query("SELECT es FROM EvaluationSnapshot es WHERE es.user = :user AND es.periodStart >= :startDate AND es.periodEnd <= :endDate ORDER BY es.periodStart DESC")
    List<EvaluationSnapshot> findByUserAndDateRange(@Param("user") User user, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT es FROM EvaluationSnapshot es WHERE es.tenant = :tenant AND es.periodLabel = :periodLabel ORDER BY es.createdAt DESC")
    List<EvaluationSnapshot> findByTenantAndPeriodLabel(@Param("tenant") Tenant tenant, @Param("periodLabel") String periodLabel);

    Boolean existsByUserAndOrganizationAndPeriodStartAndPeriodEnd(User user, Organization organization, LocalDate periodStart, LocalDate periodEnd);
}
