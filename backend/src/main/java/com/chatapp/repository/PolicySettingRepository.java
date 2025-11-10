package com.chatapp.repository;

import com.chatapp.model.Organization;
import com.chatapp.model.PolicySetting;
import com.chatapp.model.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ポリシー設定リポジトリ
 */
@Repository
public interface PolicySettingRepository extends JpaRepository<PolicySetting, Long> {

    Optional<PolicySetting> findByOrganization(Organization organization);

    List<PolicySetting> findByTenant(Tenant tenant);

    Boolean existsByOrganization(Organization organization);
}
