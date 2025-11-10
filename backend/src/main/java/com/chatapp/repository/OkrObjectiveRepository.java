package com.chatapp.repository;

import com.chatapp.model.OkrObjective;
import com.chatapp.model.Organization;
import com.chatapp.model.Tenant;
import com.chatapp.model.User;
import com.chatapp.model.enums.OkrStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * OKR目標リポジトリ
 */
@Repository
public interface OkrObjectiveRepository extends JpaRepository<OkrObjective, Long> {

    List<OkrObjective> findByTenantAndStatusOrderByCreatedAtDesc(Tenant tenant, OkrStatus status);

    List<OkrObjective> findByOrganizationAndStatusOrderByCreatedAtDesc(Organization organization, OkrStatus status);

    List<OkrObjective> findByOwnerAndStatusOrderByCreatedAtDesc(User owner, OkrStatus status);

    List<OkrObjective> findByTenantAndTargetQuarterOrderByCreatedAtDesc(Tenant tenant, String targetQuarter);

    List<OkrObjective> findByParentObjective(OkrObjective parentObjective);

    Optional<OkrObjective> findByIdAndTenant(Long id, Tenant tenant);

    @Query("SELECT o FROM OkrObjective o WHERE o.tenant = :tenant AND o.targetQuarter = :quarter AND o.status = :status ORDER BY o.createdAt DESC")
    List<OkrObjective> findByTenantAndQuarterAndStatus(@Param("tenant") Tenant tenant, @Param("quarter") String quarter, @Param("status") OkrStatus status);

    @Query("SELECT o FROM OkrObjective o WHERE o.organization = :organization AND o.targetQuarter = :quarter ORDER BY o.createdAt DESC")
    List<OkrObjective> findByOrganizationAndQuarter(@Param("organization") Organization organization, @Param("quarter") String quarter);

    @Query("SELECT o FROM OkrObjective o WHERE o.owner = :owner AND o.targetQuarter = :quarter ORDER BY o.createdAt DESC")
    List<OkrObjective> findByOwnerAndQuarter(@Param("owner") User owner, @Param("quarter") String quarter);

    Long countByOrganizationAndStatus(Organization organization, OkrStatus status);

    Long countByOwnerAndStatus(User owner, OkrStatus status);
}
