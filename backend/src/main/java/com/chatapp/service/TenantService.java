package com.chatapp.service;

import com.chatapp.model.Tenant;
import com.chatapp.model.enums.TenantStatus;
import com.chatapp.repository.TenantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * テナント管理サービス
 * テナント（企業）の作成、更新、取得などのビジネスロジックを提供
 */
@Service
@Transactional(readOnly = true)
public class TenantService {

    private static final Logger log = LoggerFactory.getLogger(TenantService.class);
    private final TenantRepository tenantRepository;

    public TenantService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    /**
     * テナント新規作成
     *
     * @param tenant 作成するテナント情報
     * @return 作成されたテナント
     * @throws IllegalArgumentException スラッグが既に存在する場合
     */
    @Transactional
    public Tenant createTenant(Tenant tenant) {
        log.info("Creating new tenant: {}", tenant.getName());

        // スラッグの重複チェック
        if (tenantRepository.findBySlug(tenant.getSlug()).isPresent()) {
            throw new IllegalArgumentException("Tenant with slug '" + tenant.getSlug() + "' already exists");
        }

        // 初期ステータス設定
        if (tenant.getStatus() == null) {
            tenant.setStatus(TenantStatus.ACTIVE);
        }

        tenant.setCreatedAt(LocalDateTime.now());
        tenant.setUpdatedAt(LocalDateTime.now());

        Tenant savedTenant = tenantRepository.save(tenant);
        log.info("Tenant created successfully with ID: {}", savedTenant.getId());

        return savedTenant;
    }

    /**
     * テナントID取得
     *
     * @param id テナントID
     * @return テナント情報
     * @throws EntityNotFoundException テナントが見つからない場合
     */
    public Tenant getTenantById(Long id) {
        log.debug("Fetching tenant by ID: {}", id);
        return tenantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found with ID: " + id));
    }

    /**
     * スラッグによるテナント取得
     *
     * @param slug テナントスラッグ
     * @return テナント情報
     */
    public Optional<Tenant> getTenantBySlug(String slug) {
        log.debug("Fetching tenant by slug: {}", slug);
        return tenantRepository.findBySlug(slug);
    }

    /**
     * テナント情報更新
     *
     * @param id テナントID
     * @param updateData 更新データ
     * @return 更新されたテナント
     * @throws EntityNotFoundException テナントが見つからない場合
     */
    @Transactional
    public Tenant updateTenant(Long id, Tenant updateData) {
        log.info("Updating tenant ID: {}", id);

        Tenant existingTenant = getTenantById(id);

        // 更新可能なフィールドのみ更新
        if (updateData.getName() != null) {
            existingTenant.setName(updateData.getName());
        }
        if (updateData.getMaxUsers() != null) {
            existingTenant.setMaxUsers(updateData.getMaxUsers());
        }
        if (updateData.getPlanType() != null) {
            existingTenant.setPlanType(updateData.getPlanType());
        }

        existingTenant.setUpdatedAt(LocalDateTime.now());

        Tenant updatedTenant = tenantRepository.save(existingTenant);
        log.info("Tenant updated successfully: {}", id);

        return updatedTenant;
    }

    /**
     * テナントステータス更新
     *
     * @param id テナントID
     * @param status 新しいステータス
     * @return 更新されたテナント
     */
    @Transactional
    public Tenant updateTenantStatus(Long id, TenantStatus status) {
        log.info("Updating tenant status - ID: {}, Status: {}", id, status);

        Tenant tenant = getTenantById(id);
        tenant.setStatus(status);
        tenant.setUpdatedAt(LocalDateTime.now());

        return tenantRepository.save(tenant);
    }

    /**
     * アクティブなテナント一覧取得
     *
     * @return アクティブなテナントリスト
     */
    public List<Tenant> getActiveTenants() {
        log.debug("Fetching all active tenants");
        return tenantRepository.findByStatus(TenantStatus.ACTIVE);
    }

    /**
     * 特定ステータスのテナント一覧取得
     *
     * @param status テナントステータス
     * @return 該当するテナントリスト
     */
    public List<Tenant> getTenantsByStatus(TenantStatus status) {
        log.debug("Fetching tenants by status: {}", status);
        return tenantRepository.findByStatus(status);
    }

    /**
     * テナントのユーザー数制限チェック
     *
     * @param tenantId テナントID
     * @param additionalUsers 追加予定のユーザー数
     * @return ユーザー追加可能かどうか
     */
    public boolean canAddUsers(Long tenantId, int additionalUsers) {
        Tenant tenant = getTenantById(tenantId);

        // 現在のユーザー数を取得（別途UserRepositoryから取得する必要がある）
        // 仮実装：現在のユーザー数は0として扱う
        int currentUsers = 0; // TODO: 実際のユーザー数を取得

        int totalUsers = currentUsers + additionalUsers;
        boolean canAdd = totalUsers <= tenant.getMaxUsers();

        if (!canAdd) {
            log.warn("Tenant {} has reached user limit. Current: {}, Max: {}",
                    tenantId, currentUsers, tenant.getMaxUsers());
        }

        return canAdd;
    }

    /**
     * テナント削除（論理削除）
     * 実際にはステータスをCLOSEDに変更
     *
     * @param id テナントID
     */
    @Transactional
    public void deleteTenant(Long id) {
        log.info("Deleting tenant ID: {}", id);
        updateTenantStatus(id, TenantStatus.CLOSED);
    }

    /**
     * テナントの存在確認
     *
     * @param id テナントID
     * @return 存在する場合true
     */
    public boolean existsById(Long id) {
        return tenantRepository.existsById(id);
    }
}
