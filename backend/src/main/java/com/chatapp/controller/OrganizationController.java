package com.chatapp.controller;

import com.chatapp.model.Organization;
import com.chatapp.model.Tenant;
import com.chatapp.security.Permission;
import com.chatapp.security.RequirePermission;
import com.chatapp.service.OrganizationService;
import com.chatapp.service.TenantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 組織のREST APIエンドポイント
 */
@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {

    private static final Logger log = LoggerFactory.getLogger(OrganizationController.class);

    private final OrganizationService organizationService;
    private final TenantService tenantService;

    public OrganizationController(
            OrganizationService organizationService,
            TenantService tenantService) {
        this.organizationService = organizationService;
        this.tenantService = tenantService;
    }

    /**
     * 組織新規作成
     * 権限: ORG_CREATE
     */
    @PostMapping
    @RequirePermission(Permission.ORG_CREATE)
    public ResponseEntity<Organization> createOrganization(@RequestBody Organization organization) {
        log.info("Creating new organization: {}", organization.getName());
        Organization created = organizationService.createOrganization(organization);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * 組織IDで取得
     */
    @GetMapping("/{organizationId}")
    public ResponseEntity<Organization> getOrganization(@PathVariable Long organizationId) {
        log.info("Fetching organization: {}", organizationId);
        Organization organization = organizationService.getOrganizationById(organizationId);
        return ResponseEntity.ok(organization);
    }

    /**
     * テナント配下の組織一覧取得
     */
    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<List<Organization>> getOrganizationsByTenant(@PathVariable Long tenantId) {
        log.info("Fetching organizations for tenant: {}", tenantId);
        Tenant tenant = tenantService.getTenantById(tenantId);
        List<Organization> organizations = organizationService.getOrganizationsByTenant(tenant);
        return ResponseEntity.ok(organizations);
    }

    /**
     * テナント配下の組織一覧取得（アクティブ状態指定）
     */
    @GetMapping("/tenant/{tenantId}/active/{isActive}")
    public ResponseEntity<List<Organization>> getOrganizations(
            @PathVariable Long tenantId,
            @PathVariable Boolean isActive) {
        log.info("Fetching organizations for tenant: {}, active: {}", tenantId, isActive);
        Tenant tenant = tenantService.getTenantById(tenantId);
        List<Organization> organizations = organizationService.getOrganizations(tenant, isActive);
        return ResponseEntity.ok(organizations);
    }

    /**
     * 親組織配下の子組織一覧取得
     */
    @GetMapping("/{organizationId}/children")
    public ResponseEntity<List<Organization>> getChildOrganizations(@PathVariable Long organizationId) {
        log.info("Fetching child organizations for parent: {}", organizationId);
        Organization parent = organizationService.getOrganizationById(organizationId);
        List<Organization> children = organizationService.getChildOrganizations(parent);
        return ResponseEntity.ok(children);
    }

    /**
     * ルート組織一覧取得
     */
    @GetMapping("/tenant/{tenantId}/roots")
    public ResponseEntity<List<Organization>> getRootOrganizations(@PathVariable Long tenantId) {
        log.info("Fetching root organizations for tenant: {}", tenantId);
        Tenant tenant = tenantService.getTenantById(tenantId);
        List<Organization> rootOrgs = organizationService.getRootOrganizations(tenant);
        return ResponseEntity.ok(rootOrgs);
    }

    /**
     * 特定レベルの組織一覧取得
     */
    @GetMapping("/tenant/{tenantId}/level/{level}")
    public ResponseEntity<List<Organization>> getOrganizationsByLevel(
            @PathVariable Long tenantId,
            @PathVariable Integer level) {
        log.info("Fetching organizations at level {} for tenant: {}", level, tenantId);
        Tenant tenant = tenantService.getTenantById(tenantId);
        List<Organization> organizations = organizationService.getOrganizationsByLevel(tenant, level);
        return ResponseEntity.ok(organizations);
    }

    /**
     * パス配下の組織検索
     */
    @GetMapping("/tenant/{tenantId}/by-path")
    public ResponseEntity<List<Organization>> getOrganizationsByPath(
            @PathVariable Long tenantId,
            @RequestParam String path) {
        log.info("Fetching organizations by path: {} for tenant: {}", path, tenantId);
        Tenant tenant = tenantService.getTenantById(tenantId);
        List<Organization> organizations = organizationService.getOrganizationsByPath(tenant, path);
        return ResponseEntity.ok(organizations);
    }

    /**
     * 組織の子孫すべて取得
     */
    @GetMapping("/{organizationId}/descendants")
    public ResponseEntity<List<Organization>> getAllDescendants(@PathVariable Long organizationId) {
        log.info("Fetching all descendants for organization: {}", organizationId);
        List<Organization> descendants = organizationService.getAllDescendants(organizationId);
        return ResponseEntity.ok(descendants);
    }

    /**
     * 組織情報更新
     * 権限: ORG_UPDATE
     */
    @PutMapping("/{organizationId}")
    @RequirePermission(Permission.ORG_UPDATE)
    public ResponseEntity<Organization> updateOrganization(
            @PathVariable Long organizationId,
            @RequestBody Organization updateData) {
        log.info("Updating organization: {}", organizationId);
        Organization updated = organizationService.updateOrganization(organizationId, updateData);
        return ResponseEntity.ok(updated);
    }

    /**
     * 組織のアクティブ状態変更
     */
    @PutMapping("/{organizationId}/active")
    public ResponseEntity<Organization> setOrganizationActive(
            @PathVariable Long organizationId,
            @RequestParam Boolean isActive) {
        log.info("Setting organization {} active status to: {}", organizationId, isActive);
        Organization updated = organizationService.setOrganizationActive(organizationId, isActive);
        return ResponseEntity.ok(updated);
    }

    /**
     * 組織削除（論理削除）
     * 権限: ORG_DELETE
     */
    @DeleteMapping("/{organizationId}")
    @RequirePermission(Permission.ORG_DELETE)
    public ResponseEntity<Void> deleteOrganization(@PathVariable Long organizationId) {
        log.info("Deleting organization: {}", organizationId);
        organizationService.deleteOrganization(organizationId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 組織の存在確認
     */
    @GetMapping("/{organizationId}/exists")
    public ResponseEntity<Boolean> existsById(@PathVariable Long organizationId) {
        log.info("Checking if organization exists: {}", organizationId);
        boolean exists = organizationService.existsById(organizationId);
        return ResponseEntity.ok(exists);
    }

    /**
     * 組織数カウント
     */
    @GetMapping("/tenant/{tenantId}/count")
    public ResponseEntity<Long> countOrganizations(@PathVariable Long tenantId) {
        log.info("Counting organizations for tenant: {}", tenantId);
        Tenant tenant = tenantService.getTenantById(tenantId);
        long count = organizationService.countOrganizations(tenant);
        return ResponseEntity.ok(count);
    }
}
