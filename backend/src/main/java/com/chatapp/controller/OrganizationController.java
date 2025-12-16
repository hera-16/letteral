package com.chatapp.controller;

import com.chatapp.dto.OrganizationTreeNode;
import com.chatapp.model.Organization;
import com.chatapp.model.OrganizationMember;
import com.chatapp.model.Tenant;
import com.chatapp.model.User;
import com.chatapp.repository.OrganizationMemberRepository;
import com.chatapp.repository.UserRepository;
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
import java.util.stream.Collectors;

/**
 * 組織のREST APIエンドポイント
 */
@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {

    private static final Logger log = LoggerFactory.getLogger(OrganizationController.class);

    private final OrganizationService organizationService;
    private final TenantService tenantService;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final UserRepository userRepository;

    public OrganizationController(
            OrganizationService organizationService,
            TenantService tenantService,
            OrganizationMemberRepository organizationMemberRepository,
            UserRepository userRepository) {
        this.organizationService = organizationService;
        this.tenantService = tenantService;
        this.organizationMemberRepository = organizationMemberRepository;
        this.userRepository = userRepository;
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

    /**
     * 組織ツリー構造を取得
     */
    @GetMapping("/tenant/{tenantId}/tree")
    public ResponseEntity<List<OrganizationTreeNode>> getOrganizationTree(@PathVariable Long tenantId) {
        log.info("Fetching organization tree for tenant: {}", tenantId);
        Tenant tenant = tenantService.getTenantById(tenantId);
        List<OrganizationTreeNode> tree = organizationService.getOrganizationTree(tenant);
        return ResponseEntity.ok(tree);
    }

    /**
     * 特定組織配下のサブツリーを取得
     */
    @GetMapping("/{organizationId}/tree")
    public ResponseEntity<OrganizationTreeNode> getOrganizationSubTree(@PathVariable Long organizationId) {
        log.info("Fetching organization sub-tree for organization: {}", organizationId);
        OrganizationTreeNode subTree = organizationService.getOrganizationSubTree(organizationId);
        return ResponseEntity.ok(subTree);
    }

    /**
     * ユーザーが所属する組織一覧を取得
     * ユーザーが投稿可能な組織を返す
     * CEO/SUPER_ADMINの場合はテナント内の全組織を返す
     */
    @GetMapping("/user/{userId}/accessible")
    public ResponseEntity<List<Organization>> getUserAccessibleOrganizations(@PathVariable Long userId) {
        log.info("Fetching accessible organizations for user: {}", userId);

        // ユーザーの存在確認と取得
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // CEO/SUPER_ADMIN権限チェック（全組織を返す）
        String userRole = user.getRole();
        log.info("User {} role: '{}' (comparing with TENANT_ADMIN and SUPER_ADMIN)", userId, userRole);
        if ("TENANT_ADMIN".equals(userRole) || "SUPER_ADMIN".equals(userRole)) {
            log.info("User {} is TENANT_ADMIN/SUPER_ADMIN - returning all organizations in tenant", userId);
            Tenant tenant = tenantService.getTenantById(user.getTenantId());
            List<Organization> allOrganizations = organizationService.getOrganizationsByTenant(tenant);
            log.info("Found {} organizations for TENANT_ADMIN/SUPER_ADMIN user {}", allOrganizations.size(), userId);
            return ResponseEntity.ok(allOrganizations);
        }

        // 通常ユーザー: 所属する組織を取得
        List<Organization> organizations = organizationMemberRepository.findByUserId(userId)
                .stream()
                .map(OrganizationMember::getOrganization)
                .filter(org -> org.getIsActive() != null && org.getIsActive()) // アクティブな組織のみ
                .collect(Collectors.toList());

        log.info("Found {} accessible organizations for user {}", organizations.size(), userId);
        return ResponseEntity.ok(organizations);
    }
}
