package com.chatapp.repository;

import com.chatapp.model.SlackIntegration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SlackIntegrationRepository extends JpaRepository<SlackIntegration, Long> {

    List<SlackIntegration> findByTenantId(Long tenantId);

    Optional<SlackIntegration> findByTenantIdAndIsActive(Long tenantId, Boolean isActive);

    Optional<SlackIntegration> findByTenantIdAndWorkspaceId(Long tenantId, String workspaceId);
}
