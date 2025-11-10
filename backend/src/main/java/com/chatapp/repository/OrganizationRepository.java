package com.chatapp.repository;

import com.chatapp.model.Organization;
import com.chatapp.model.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 組織リポジトリ
 */
@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    List<Organization> findByTenantAndIsActiveOrderByDisplayOrder(Tenant tenant, Boolean isActive);

    List<Organization> findByTenantAndParentIsNullAndIsActive(Tenant tenant, Boolean isActive);

    List<Organization> findByParentAndIsActiveOrderByDisplayOrder(Organization parent, Boolean isActive);

    Optional<Organization> findByIdAndTenant(Long id, Tenant tenant);

    @Query("SELECT o FROM Organization o WHERE o.tenant = :tenant AND o.level = :level AND o.isActive = true ORDER BY o.displayOrder")
    List<Organization> findByTenantAndLevel(@Param("tenant") Tenant tenant, @Param("level") Integer level);

    @Query("SELECT o FROM Organization o WHERE o.tenant = :tenant AND o.path LIKE :path% AND o.isActive = true ORDER BY o.level, o.displayOrder")
    List<Organization> findByTenantAndPathStartingWith(@Param("tenant") Tenant tenant, @Param("path") String path);
}
