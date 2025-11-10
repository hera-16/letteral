package com.chatapp.repository;

import com.chatapp.model.Tenant;
import com.chatapp.model.enums.TenantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * テナントリポジトリ
 */
@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {

    Optional<Tenant> findBySlug(String slug);

    Boolean existsBySlug(String slug);

    List<Tenant> findByStatus(TenantStatus status);

    List<Tenant> findByStatusOrderByCreatedAtDesc(TenantStatus status);
}
