package com.chatapp.service;

import com.chatapp.model.OkrObjective;
import com.chatapp.model.Organization;
import com.chatapp.model.Tenant;
import com.chatapp.model.User;
import com.chatapp.model.enums.OkrStatus;
import com.chatapp.repository.OkrObjectiveRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * OKR Objectiveサービス
 * Objectiveの作成、更新、削除、進捗管理などのビジネスロジックを提供
 */
@Service
@Transactional(readOnly = true)
public class OkrObjectiveService {

    private static final Logger log = LoggerFactory.getLogger(OkrObjectiveService.class);
    private final OkrObjectiveRepository okrObjectiveRepository;

    public OkrObjectiveService(OkrObjectiveRepository okrObjectiveRepository) {
        this.okrObjectiveRepository = okrObjectiveRepository;
    }

    /**
     * Objective作成
     *
     * @param objective 作成するObjective
     * @return 作成されたObjective
     */
    @Transactional
    public OkrObjective createObjective(OkrObjective objective) {
        log.info("Creating new OKR objective: {}", objective.getTitle());

        objective.setCreatedAt(LocalDateTime.now());
        objective.setUpdatedAt(LocalDateTime.now());

        if (objective.getProgressRate() == null) {
            objective.setProgressRate(0);
        }

        OkrObjective saved = okrObjectiveRepository.save(objective);
        log.info("OKR objective created successfully with ID: {}", saved.getId());

        return saved;
    }

    /**
     * ObjectiveID取得
     *
     * @param id ObjectiveID
     * @return Objective情報
     * @throws EntityNotFoundException Objectiveが見つからない場合
     */
    public OkrObjective getObjectiveById(Long id) {
        log.debug("Fetching OKR objective by ID: {}", id);
        return okrObjectiveRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("OKR objective not found with ID: " + id));
    }

    /**
     * テナント×ステータスでObjective取得
     *
     * @param tenant テナント
     * @param status ステータス
     * @return Objectiveリスト
     */
    public List<OkrObjective> getObjectivesByTenantAndStatus(Tenant tenant, OkrStatus status) {
        log.debug("Fetching objectives with status {} for tenant {}", status, tenant.getName());
        return okrObjectiveRepository.findByTenantAndStatusOrderByCreatedAtDesc(tenant, status);
    }

    /**
     * 組織×ステータスでObjective取得
     *
     * @param organization 組織
     * @param status ステータス
     * @return Objectiveリスト
     */
    public List<OkrObjective> getObjectivesByOrganizationAndStatus(Organization organization, OkrStatus status) {
        log.debug("Fetching objectives with status {} for organization {}", status, organization.getName());
        return okrObjectiveRepository.findByOrganizationAndStatusOrderByCreatedAtDesc(organization, status);
    }

    /**
     * オーナー×ステータスでObjective取得
     *
     * @param owner オーナー
     * @param status ステータス
     * @return Objectiveリスト
     */
    public List<OkrObjective> getObjectivesByOwnerAndStatus(User owner, OkrStatus status) {
        log.debug("Fetching objectives with status {} for owner {}", status, owner.getId());
        return okrObjectiveRepository.findByOwnerAndStatusOrderByCreatedAtDesc(owner, status);
    }

    /**
     * 四半期でフィルタリング
     *
     * @param tenant テナント
     * @param targetQuarter 四半期（例: "2024_Q1"）
     * @return Objectiveリスト
     */
    public List<OkrObjective> getObjectivesByQuarter(Tenant tenant, String targetQuarter) {
        log.debug("Fetching objectives for quarter {} in tenant {}", targetQuarter, tenant.getName());
        return okrObjectiveRepository.findByTenantAndTargetQuarterOrderByCreatedAtDesc(tenant, targetQuarter);
    }

    /**
     * 四半期×ステータスでフィルタリング
     *
     * @param tenant テナント
     * @param targetQuarter 四半期
     * @param status ステータス
     * @return Objectiveリスト
     */
    public List<OkrObjective> getObjectivesByQuarterAndStatus(Tenant tenant, String targetQuarter, OkrStatus status) {
        log.debug("Fetching objectives for quarter {} with status {} in tenant {}",
                  targetQuarter, status, tenant.getName());
        return okrObjectiveRepository.findByTenantAndQuarterAndStatus(tenant, targetQuarter, status);
    }

    /**
     * 組織×四半期でObjective取得
     *
     * @param organization 組織
     * @param targetQuarter 四半期
     * @return Objectiveリスト
     */
    public List<OkrObjective> getObjectivesByOrganizationAndQuarter(Organization organization, String targetQuarter) {
        log.debug("Fetching objectives for quarter {} in organization {}", targetQuarter, organization.getName());
        return okrObjectiveRepository.findByOrganizationAndQuarter(organization, targetQuarter);
    }

    /**
     * オーナー×四半期でObjective取得
     *
     * @param owner オーナー
     * @param targetQuarter 四半期
     * @return Objectiveリスト
     */
    public List<OkrObjective> getObjectivesByOwnerAndQuarter(User owner, String targetQuarter) {
        log.debug("Fetching objectives for quarter {} for owner {}", targetQuarter, owner.getId());
        return okrObjectiveRepository.findByOwnerAndQuarter(owner, targetQuarter);
    }

    /**
     * 子Objective一覧取得
     *
     * @param parentObjective 親Objective
     * @return 子Objectiveリスト
     */
    public List<OkrObjective> getChildObjectives(OkrObjective parentObjective) {
        log.debug("Fetching child objectives for parent: {}", parentObjective.getId());
        return okrObjectiveRepository.findByParentObjective(parentObjective);
    }

    /**
     * Objective更新
     *
     * @param id ObjectiveID
     * @param updateData 更新データ
     * @return 更新されたObjective
     */
    @Transactional
    public OkrObjective updateObjective(Long id, OkrObjective updateData) {
        log.info("Updating OKR objective ID: {}", id);

        OkrObjective existing = getObjectiveById(id);

        if (updateData.getTitle() != null) {
            existing.setTitle(updateData.getTitle());
        }
        if (updateData.getDescription() != null) {
            existing.setDescription(updateData.getDescription());
        }
        if (updateData.getTargetQuarter() != null) {
            existing.setTargetQuarter(updateData.getTargetQuarter());
        }
        if (updateData.getStartDate() != null) {
            existing.setStartDate(updateData.getStartDate());
        }
        if (updateData.getEndDate() != null) {
            existing.setEndDate(updateData.getEndDate());
        }
        if (updateData.getStatus() != null) {
            existing.setStatus(updateData.getStatus());
        }

        existing.setUpdatedAt(LocalDateTime.now());

        OkrObjective updated = okrObjectiveRepository.save(existing);
        log.info("OKR objective updated successfully: {}", id);

        return updated;
    }

    /**
     * 進捗率更新
     *
     * @param id ObjectiveID
     * @param progressRate 進捗率（0-100）
     * @return 更新されたObjective
     */
    @Transactional
    public OkrObjective updateProgress(Long id, Integer progressRate) {
        log.info("Updating progress for objective {} to {}%", id, progressRate);

        if (progressRate < 0 || progressRate > 100) {
            throw new IllegalArgumentException("Progress rate must be between 0 and 100");
        }

        OkrObjective objective = getObjectiveById(id);
        objective.setProgressRate(progressRate);
        objective.setUpdatedAt(LocalDateTime.now());

        // 進捗率100%の場合、ステータスを完了に変更
        if (progressRate == 100 && objective.getStatus() == OkrStatus.ACTIVE) {
            objective.setStatus(OkrStatus.COMPLETED);
        }

        return okrObjectiveRepository.save(objective);
    }

    /**
     * ステータス更新
     *
     * @param id ObjectiveID
     * @param status 新しいステータス
     * @return 更新されたObjective
     */
    @Transactional
    public OkrObjective updateStatus(Long id, OkrStatus status) {
        log.info("Updating status for objective {} to {}", id, status);

        OkrObjective objective = getObjectiveById(id);
        objective.setStatus(status);
        objective.setUpdatedAt(LocalDateTime.now());

        return okrObjectiveRepository.save(objective);
    }

    /**
     * Objective削除
     *
     * @param id ObjectiveID
     */
    @Transactional
    public void deleteObjective(Long id) {
        log.info("Deleting OKR objective ID: {}", id);

        // 子Objectiveの存在チェック
        OkrObjective objective = getObjectiveById(id);
        List<OkrObjective> children = getChildObjectives(objective);
        if (!children.isEmpty()) {
            throw new IllegalStateException("Cannot delete objective with child objectives");
        }

        okrObjectiveRepository.deleteById(id);
        log.info("OKR objective deleted successfully: {}", id);
    }

    /**
     * 組織のアクティブなObjective数カウント
     *
     * @param organization 組織
     * @return アクティブなObjective数
     */
    public long countActiveObjectivesByOrganization(Organization organization) {
        return okrObjectiveRepository.countByOrganizationAndStatus(organization, OkrStatus.ACTIVE);
    }

    /**
     * オーナーのアクティブなObjective数カウント
     *
     * @param owner オーナー
     * @return アクティブなObjective数
     */
    public long countActiveObjectivesByOwner(User owner) {
        return okrObjectiveRepository.countByOwnerAndStatus(owner, OkrStatus.ACTIVE);
    }
}
