package com.chatapp.repository;

import com.chatapp.model.AnonymousProfile;
import com.chatapp.model.Organization;
import com.chatapp.model.Tenant;
import com.chatapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 匿名プロフィールリポジトリ
 */
@Repository
public interface AnonymousProfileRepository extends JpaRepository<AnonymousProfile, Long> {

    Optional<AnonymousProfile> findByOrganizationAndUser(Organization organization, User user);

    Optional<AnonymousProfile> findByTenantAndAnonymousId(Tenant tenant, String anonymousId);

    List<AnonymousProfile> findByUser(User user);

    List<AnonymousProfile> findByOrganization(Organization organization);

    Boolean existsByOrganizationAndUser(Organization organization, User user);

    Boolean existsByTenantAndAnonymousId(Tenant tenant, String anonymousId);
}
