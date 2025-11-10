package com.chatapp.repository;

import com.chatapp.model.Organization;
import com.chatapp.model.OrganizationMember;
import com.chatapp.model.Tenant;
import com.chatapp.model.User;
import com.chatapp.model.enums.OrganizationRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 組織メンバーシップリポジトリ
 */
@Repository
public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, Long> {

    List<OrganizationMember> findByOrganization(Organization organization);

    List<OrganizationMember> findByUser(User user);

    List<OrganizationMember> findByTenantAndUser(Tenant tenant, User user);

    Optional<OrganizationMember> findByOrganizationAndUser(Organization organization, User user);

    Boolean existsByOrganizationAndUser(Organization organization, User user);

    Optional<OrganizationMember> findByUserAndIsPrimary(User user, Boolean isPrimary);

    List<OrganizationMember> findByOrganizationAndRole(Organization organization, OrganizationRole role);

    @Query("SELECT om FROM OrganizationMember om WHERE om.tenant = :tenant AND om.user = :user AND om.role IN :roles")
    List<OrganizationMember> findByTenantAndUserAndRoleIn(@Param("tenant") Tenant tenant, @Param("user") User user, @Param("roles") List<OrganizationRole> roles);

    Long countByOrganization(Organization organization);
}
