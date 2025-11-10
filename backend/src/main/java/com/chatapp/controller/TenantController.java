package com.chatapp.controller;

import com.chatapp.model.Tenant;
import com.chatapp.model.enums.TenantStatus;
import com.chatapp.service.TenantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * テナントのREST APIエンドポイント
 */
@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    private static final Logger log = LoggerFactory.getLogger(TenantController.class);

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    /**
     * テナント新規作成
     */
    @PostMapping
    public ResponseEntity<Tenant> createTenant(@RequestBody Tenant tenant) {
        log.info("Creating new tenant: {}", tenant.getName());
        Tenant created = tenantService.createTenant(tenant);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * テナントIDで取得
     */
    @GetMapping("/{tenantId}")
    public ResponseEntity<Tenant> getTenant(@PathVariable Long tenantId) {
        log.info("Fetching tenant: {}", tenantId);
        Tenant tenant = tenantService.getTenantById(tenantId);
        return ResponseEntity.ok(tenant);
    }

    /**
     * スラッグによるテナント取得
     */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<Tenant> getTenantBySlug(@PathVariable String slug) {
        log.info("Fetching tenant by slug: {}", slug);
        Optional<Tenant> tenant = tenantService.getTenantBySlug(slug);
        return tenant.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * アクティブなテナント一覧取得
     */
    @GetMapping("/active")
    public ResponseEntity<List<Tenant>> getActiveTenants() {
        log.info("Fetching all active tenants");
        List<Tenant> tenants = tenantService.getActiveTenants();
        return ResponseEntity.ok(tenants);
    }

    /**
     * 特定ステータスのテナント一覧取得
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Tenant>> getTenantsByStatus(@PathVariable TenantStatus status) {
        log.info("Fetching tenants by status: {}", status);
        List<Tenant> tenants = tenantService.getTenantsByStatus(status);
        return ResponseEntity.ok(tenants);
    }

    /**
     * テナント情報更新
     */
    @PutMapping("/{tenantId}")
    public ResponseEntity<Tenant> updateTenant(
            @PathVariable Long tenantId,
            @RequestBody Tenant updateData) {
        log.info("Updating tenant: {}", tenantId);
        Tenant updated = tenantService.updateTenant(tenantId, updateData);
        return ResponseEntity.ok(updated);
    }

    /**
     * テナントステータス更新
     */
    @PutMapping("/{tenantId}/status")
    public ResponseEntity<Tenant> updateTenantStatus(
            @PathVariable Long tenantId,
            @RequestParam TenantStatus status) {
        log.info("Updating tenant status - ID: {}, Status: {}", tenantId, status);
        Tenant updated = tenantService.updateTenantStatus(tenantId, status);
        return ResponseEntity.ok(updated);
    }

    /**
     * テナントのユーザー数制限チェック
     */
    @GetMapping("/{tenantId}/can-add-users")
    public ResponseEntity<Boolean> canAddUsers(
            @PathVariable Long tenantId,
            @RequestParam int additionalUsers) {
        log.info("Checking if tenant {} can add {} users", tenantId, additionalUsers);
        boolean canAdd = tenantService.canAddUsers(tenantId, additionalUsers);
        return ResponseEntity.ok(canAdd);
    }

    /**
     * テナント削除（論理削除）
     */
    @DeleteMapping("/{tenantId}")
    public ResponseEntity<Void> deleteTenant(@PathVariable Long tenantId) {
        log.info("Deleting tenant: {}", tenantId);
        tenantService.deleteTenant(tenantId);
        return ResponseEntity.noContent().build();
    }

    /**
     * テナントの存在確認
     */
    @GetMapping("/{tenantId}/exists")
    public ResponseEntity<Boolean> existsById(@PathVariable Long tenantId) {
        log.info("Checking if tenant exists: {}", tenantId);
        boolean exists = tenantService.existsById(tenantId);
        return ResponseEntity.ok(exists);
    }
}
