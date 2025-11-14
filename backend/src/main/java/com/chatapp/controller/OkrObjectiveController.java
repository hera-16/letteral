package com.chatapp.controller;

import com.chatapp.model.OkrObjective;
import com.chatapp.model.Organization;
import com.chatapp.model.Tenant;
import com.chatapp.model.User;
import com.chatapp.model.enums.OkrStatus;
import com.chatapp.security.Permission;
import com.chatapp.security.RequirePermission;
import com.chatapp.service.OkrObjectiveService;
import com.chatapp.service.OrganizationService;
import com.chatapp.service.TenantService;
import com.chatapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;

/**
 * OKR ObjectiveのREST APIエンドポイント
 */
@RestController
@RequestMapping("/api/okr-objectives")
public class OkrObjectiveController {

    private static final Logger log = LoggerFactory.getLogger(OkrObjectiveController.class);

    private final OkrObjectiveService okrObjectiveService;
    private final OrganizationService organizationService;
    private final TenantService tenantService;
    private final UserRepository userRepository;

    public OkrObjectiveController(
            OkrObjectiveService okrObjectiveService,
            OrganizationService organizationService,
            TenantService tenantService,
            UserRepository userRepository) {
        this.okrObjectiveService = okrObjectiveService;
        this.organizationService = organizationService;
        this.tenantService = tenantService;
        this.userRepository = userRepository;
    }

    /**
     * Objective作成
     * 権限: OKR_CREATE
     */
    @PostMapping
    @RequirePermission(Permission.OKR_CREATE)
    public ResponseEntity<OkrObjective> createObjective(@RequestBody OkrObjective objective) {
        log.info("Creating OKR objective: {}", objective.getTitle());
        OkrObjective created = okrObjectiveService.createObjective(objective);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * ObjectiveIDで取得
     */
    @GetMapping("/{objectiveId}")
    public ResponseEntity<OkrObjective> getObjective(@PathVariable Long objectiveId) {
        log.info("Fetching OKR objective: {}", objectiveId);
        OkrObjective objective = okrObjectiveService.getObjectiveById(objectiveId);
        return ResponseEntity.ok(objective);
    }

    /**
     * テナント×ステータスでObjective取得
     */
    @GetMapping("/tenant/{tenantId}/status/{status}")
    public ResponseEntity<List<OkrObjective>> getObjectivesByTenantAndStatus(
            @PathVariable Long tenantId,
            @PathVariable OkrStatus status) {
        log.info("Fetching objectives with status {} for tenant {}", status, tenantId);
        Tenant tenant = tenantService.getTenantById(tenantId);
        List<OkrObjective> objectives = okrObjectiveService.getObjectivesByTenantAndStatus(tenant, status);
        return ResponseEntity.ok(objectives);
    }

    /**
     * 組織×ステータスでObjective取得
     */
    @GetMapping("/organization/{organizationId}/status/{status}")
    public ResponseEntity<List<OkrObjective>> getObjectivesByOrganizationAndStatus(
            @PathVariable Long organizationId,
            @PathVariable OkrStatus status) {
        log.info("Fetching objectives with status {} for organization {}", status, organizationId);
        Organization organization = organizationService.getOrganizationById(organizationId);
        List<OkrObjective> objectives = okrObjectiveService.getObjectivesByOrganizationAndStatus(organization, status);
        return ResponseEntity.ok(objectives);
    }

    /**
     * オーナー×ステータスでObjective取得
     */
    @GetMapping("/owner/{ownerId}/status/{status}")
    public ResponseEntity<List<OkrObjective>> getObjectivesByOwnerAndStatus(
            @PathVariable Long ownerId,
            @PathVariable OkrStatus status) {
        log.info("Fetching objectives with status {} for owner {}", status, ownerId);
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + ownerId));
        List<OkrObjective> objectives = okrObjectiveService.getObjectivesByOwnerAndStatus(owner, status);
        return ResponseEntity.ok(objectives);
    }

    /**
     * 四半期でフィルタリング
     */
    @GetMapping("/tenant/{tenantId}/quarter/{targetQuarter}")
    public ResponseEntity<List<OkrObjective>> getObjectivesByQuarter(
            @PathVariable Long tenantId,
            @PathVariable String targetQuarter) {
        log.info("Fetching objectives for quarter {} in tenant {}", targetQuarter, tenantId);
        Tenant tenant = tenantService.getTenantById(tenantId);
        List<OkrObjective> objectives = okrObjectiveService.getObjectivesByQuarter(tenant, targetQuarter);
        return ResponseEntity.ok(objectives);
    }

    /**
     * 四半期×ステータスでフィルタリング
     */
    @GetMapping("/tenant/{tenantId}/quarter/{targetQuarter}/status/{status}")
    public ResponseEntity<List<OkrObjective>> getObjectivesByQuarterAndStatus(
            @PathVariable Long tenantId,
            @PathVariable String targetQuarter,
            @PathVariable OkrStatus status) {
        log.info("Fetching objectives for quarter {} with status {} in tenant {}", targetQuarter, status, tenantId);
        Tenant tenant = tenantService.getTenantById(tenantId);
        List<OkrObjective> objectives = okrObjectiveService.getObjectivesByQuarterAndStatus(tenant, targetQuarter, status);
        return ResponseEntity.ok(objectives);
    }

    /**
     * 組織×四半期でObjective取得
     */
    @GetMapping("/organization/{organizationId}/quarter/{targetQuarter}")
    public ResponseEntity<List<OkrObjective>> getObjectivesByOrganizationAndQuarter(
            @PathVariable Long organizationId,
            @PathVariable String targetQuarter) {
        log.info("Fetching objectives for quarter {} in organization {}", targetQuarter, organizationId);
        Organization organization = organizationService.getOrganizationById(organizationId);
        List<OkrObjective> objectives = okrObjectiveService.getObjectivesByOrganizationAndQuarter(organization, targetQuarter);
        return ResponseEntity.ok(objectives);
    }

    /**
     * オーナー×四半期でObjective取得
     */
    @GetMapping("/owner/{ownerId}/quarter/{targetQuarter}")
    public ResponseEntity<List<OkrObjective>> getObjectivesByOwnerAndQuarter(
            @PathVariable Long ownerId,
            @PathVariable String targetQuarter) {
        log.info("Fetching objectives for quarter {} for owner {}", targetQuarter, ownerId);
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + ownerId));
        List<OkrObjective> objectives = okrObjectiveService.getObjectivesByOwnerAndQuarter(owner, targetQuarter);
        return ResponseEntity.ok(objectives);
    }

    /**
     * 子Objective一覧取得
     */
    @GetMapping("/{objectiveId}/children")
    public ResponseEntity<List<OkrObjective>> getChildObjectives(@PathVariable Long objectiveId) {
        log.info("Fetching child objectives for parent: {}", objectiveId);
        OkrObjective parent = okrObjectiveService.getObjectiveById(objectiveId);
        List<OkrObjective> children = okrObjectiveService.getChildObjectives(parent);
        return ResponseEntity.ok(children);
    }

    /**
     * Objective更新
     * 権限: OKR_UPDATE
     */
    @PutMapping("/{objectiveId}")
    @RequirePermission(Permission.OKR_UPDATE)
    public ResponseEntity<OkrObjective> updateObjective(
            @PathVariable Long objectiveId,
            @RequestBody OkrObjective updateData) {
        log.info("Updating OKR objective: {}", objectiveId);
        OkrObjective updated = okrObjectiveService.updateObjective(objectiveId, updateData);
        return ResponseEntity.ok(updated);
    }

    /**
     * 進捗率更新
     */
    @PutMapping("/{objectiveId}/progress")
    public ResponseEntity<OkrObjective> updateProgress(
            @PathVariable Long objectiveId,
            @RequestParam Integer progressRate) {
        log.info("Updating progress for objective {} to {}%", objectiveId, progressRate);
        OkrObjective updated = okrObjectiveService.updateProgress(objectiveId, progressRate);
        return ResponseEntity.ok(updated);
    }

    /**
     * ステータス更新
     */
    @PutMapping("/{objectiveId}/status")
    public ResponseEntity<OkrObjective> updateStatus(
            @PathVariable Long objectiveId,
            @RequestParam OkrStatus status) {
        log.info("Updating status for objective {} to {}", objectiveId, status);
        OkrObjective updated = okrObjectiveService.updateStatus(objectiveId, status);
        return ResponseEntity.ok(updated);
    }

    /**
     * Objective削除
     * 権限: OKR_DELETE
     */
    @DeleteMapping("/{objectiveId}")
    @RequirePermission(Permission.OKR_DELETE)
    public ResponseEntity<Void> deleteObjective(@PathVariable Long objectiveId) {
        log.info("Deleting OKR objective: {}", objectiveId);
        okrObjectiveService.deleteObjective(objectiveId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 組織のアクティブなObjective数カウント
     */
    @GetMapping("/organization/{organizationId}/active/count")
    public ResponseEntity<Long> countActiveObjectivesByOrganization(@PathVariable Long organizationId) {
        log.info("Counting active objectives for organization: {}", organizationId);
        Organization organization = organizationService.getOrganizationById(organizationId);
        long count = okrObjectiveService.countActiveObjectivesByOrganization(organization);
        return ResponseEntity.ok(count);
    }

    /**
     * オーナーのアクティブなObjective数カウント
     */
    @GetMapping("/owner/{ownerId}/active/count")
    public ResponseEntity<Long> countActiveObjectivesByOwner(@PathVariable Long ownerId) {
        log.info("Counting active objectives for owner: {}", ownerId);
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + ownerId));
        long count = okrObjectiveService.countActiveObjectivesByOwner(owner);
        return ResponseEntity.ok(count);
    }
}
