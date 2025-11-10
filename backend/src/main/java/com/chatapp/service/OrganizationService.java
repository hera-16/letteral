package com.chatapp.service;

import com.chatapp.model.Organization;
import com.chatapp.model.Tenant;
import com.chatapp.repository.OrganizationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 組織管理サービス
 * 組織の作成、更新、階層構造管理などのビジネスロジックを提供
 */
@Service
@Transactional(readOnly = true)
public class OrganizationService {

    private static final Logger log = LoggerFactory.getLogger(OrganizationService.class);
    private final OrganizationRepository organizationRepository;

    public OrganizationService(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    /**
     * 組織新規作成
     *
     * @param organization 作成する組織情報
     * @return 作成された組織
     */
    @Transactional
    public Organization createOrganization(Organization organization) {
        log.info("Creating new organization: {}", organization.getName());

        // 親組織が指定されている場合、階層レベルとパスを計算
        if (organization.getParent() != null) {
            Organization parent = getOrganizationById(organization.getParent().getId());
            organization.setLevel(parent.getLevel() + 1);
            organization.setPath(parent.getPath() + "/" + organization.getName());
        } else {
            // ルート組織の場合
            organization.setLevel(0);
            organization.setPath("/" + organization.getName());
        }

        organization.setCreatedAt(LocalDateTime.now());
        organization.setUpdatedAt(LocalDateTime.now());

        Organization savedOrganization = organizationRepository.save(organization);
        log.info("Organization created successfully with ID: {}", savedOrganization.getId());

        return savedOrganization;
    }

    /**
     * 組織ID取得
     *
     * @param id 組織ID
     * @return 組織情報
     * @throws EntityNotFoundException 組織が見つからない場合
     */
    public Organization getOrganizationById(Long id) {
        log.debug("Fetching organization by ID: {}", id);
        return organizationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found with ID: " + id));
    }

    /**
     * テナント配下のアクティブな組織取得
     *
     * @param tenant テナント
     * @return 組織リスト
     */
    public List<Organization> getOrganizationsByTenant(Tenant tenant) {
        log.debug("Fetching active organizations for tenant: {}", tenant.getName());
        return organizationRepository.findByTenantAndIsActiveOrderByDisplayOrder(tenant, true);
    }

    /**
     * アクティブな組織一覧取得
     *
     * @param tenant テナント
     * @param isActive アクティブかどうか
     * @return 組織リスト
     */
    public List<Organization> getOrganizations(Tenant tenant, Boolean isActive) {
        log.debug("Fetching organizations for tenant: {}, active: {}", tenant.getName(), isActive);
        return organizationRepository.findByTenantAndIsActiveOrderByDisplayOrder(tenant, isActive);
    }

    /**
     * 親組織配下の子組織一覧取得
     *
     * @param parent 親組織
     * @return 子組織リスト
     */
    public List<Organization> getChildOrganizations(Organization parent) {
        log.debug("Fetching child organizations for parent: {}", parent.getName());
        return organizationRepository.findByParentAndIsActiveOrderByDisplayOrder(parent, true);
    }

    /**
     * ルート組織一覧取得
     *
     * @param tenant テナント
     * @return ルート組織リスト
     */
    public List<Organization> getRootOrganizations(Tenant tenant) {
        log.debug("Fetching root organizations for tenant: {}", tenant.getName());
        return organizationRepository.findByTenantAndParentIsNullAndIsActive(tenant, true);
    }

    /**
     * 特定レベルの組織一覧取得
     *
     * @param tenant テナント
     * @param level 階層レベル
     * @return 組織リスト
     */
    public List<Organization> getOrganizationsByLevel(Tenant tenant, Integer level) {
        log.debug("Fetching organizations at level {} for tenant: {}", level, tenant.getName());
        return organizationRepository.findByTenantAndLevel(tenant, level);
    }

    /**
     * パス配下の組織検索
     *
     * @param tenant テナント
     * @param path パス
     * @return 組織リスト
     */
    public List<Organization> getOrganizationsByPath(Tenant tenant, String path) {
        log.debug("Fetching organizations by path: {} for tenant: {}", path, tenant.getName());
        return organizationRepository.findByTenantAndPathStartingWith(tenant, path);
    }

    /**
     * 組織情報更新
     *
     * @param id 組織ID
     * @param updateData 更新データ
     * @return 更新された組織
     * @throws EntityNotFoundException 組織が見つからない場合
     */
    @Transactional
    public Organization updateOrganization(Long id, Organization updateData) {
        log.info("Updating organization ID: {}", id);

        Organization existingOrg = getOrganizationById(id);

        // 更新可能なフィールドのみ更新
        if (updateData.getName() != null) {
            existingOrg.setName(updateData.getName());
            // 名前が変わった場合、パスも更新
            updateOrganizationPath(existingOrg);
        }
        if (updateData.getDescription() != null) {
            existingOrg.setDescription(updateData.getDescription());
        }
        if (updateData.getOrganizationType() != null) {
            existingOrg.setOrganizationType(updateData.getOrganizationType());
        }
        if (updateData.getDisplayOrder() != null) {
            existingOrg.setDisplayOrder(updateData.getDisplayOrder());
        }

        existingOrg.setUpdatedAt(LocalDateTime.now());

        Organization updatedOrg = organizationRepository.save(existingOrg);
        log.info("Organization updated successfully: {}", id);

        return updatedOrg;
    }

    /**
     * 組織のパス更新（名前変更時に使用）
     *
     * @param organization 組織
     */
    private void updateOrganizationPath(Organization organization) {
        if (organization.getParent() != null) {
            organization.setPath(organization.getParent().getPath() + "/" + organization.getName());
        } else {
            organization.setPath("/" + organization.getName());
        }
    }

    /**
     * 組織のアクティブ状態変更
     *
     * @param id 組織ID
     * @param isActive アクティブかどうか
     * @return 更新された組織
     */
    @Transactional
    public Organization setOrganizationActive(Long id, Boolean isActive) {
        log.info("Setting organization {} active status to: {}", id, isActive);

        Organization organization = getOrganizationById(id);
        organization.setIsActive(isActive);
        organization.setUpdatedAt(LocalDateTime.now());

        return organizationRepository.save(organization);
    }

    /**
     * 組織削除（論理削除）
     *
     * @param id 組織ID
     */
    @Transactional
    public void deleteOrganization(Long id) {
        log.info("Deleting organization ID: {}", id);

        // 子組織の存在チェック
        Organization org = getOrganizationById(id);
        List<Organization> children = getChildOrganizations(org);
        if (!children.isEmpty()) {
            throw new IllegalStateException("Cannot delete organization with child organizations. " +
                    "Please delete or move child organizations first.");
        }

        setOrganizationActive(id, false);
    }

    /**
     * 組織の存在確認
     *
     * @param id 組織ID
     * @return 存在する場合true
     */
    public boolean existsById(Long id) {
        return organizationRepository.existsById(id);
    }

    /**
     * 組織階層のカウント取得
     *
     * @param tenant テナント
     * @return 組織数
     */
    public long countOrganizations(Tenant tenant) {
        log.debug("Counting organizations for tenant: {}", tenant.getName());
        return organizationRepository.findByTenantAndIsActiveOrderByDisplayOrder(tenant, true).size();
    }

    /**
     * 組織の子孫すべて取得（再帰的）
     *
     * @param organizationId 組織ID
     * @return 子孫組織リスト
     */
    public List<Organization> getAllDescendants(Long organizationId) {
        log.debug("Fetching all descendants for organization ID: {}", organizationId);
        Organization org = getOrganizationById(organizationId);
        return organizationRepository.findByTenantAndPathStartingWith(org.getTenant(), org.getPath() + "/");
    }
}
