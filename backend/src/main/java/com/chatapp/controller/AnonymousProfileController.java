package com.chatapp.controller;

import com.chatapp.model.AnonymousProfile;
import com.chatapp.model.Organization;
import com.chatapp.model.Tenant;
import com.chatapp.model.User;
import com.chatapp.service.AnonymousProfileService;
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
import java.util.Optional;

/**
 * 匿名プロファイルのREST APIエンドポイント
 */
@RestController
@RequestMapping("/api/anonymous-profiles")
public class AnonymousProfileController {

    private static final Logger log = LoggerFactory.getLogger(AnonymousProfileController.class);

    private final AnonymousProfileService anonymousProfileService;
    private final OrganizationService organizationService;
    private final TenantService tenantService;
    private final UserRepository userRepository;

    public AnonymousProfileController(
            AnonymousProfileService anonymousProfileService,
            OrganizationService organizationService,
            TenantService tenantService,
            UserRepository userRepository) {
        this.anonymousProfileService = anonymousProfileService;
        this.organizationService = organizationService;
        this.tenantService = tenantService;
        this.userRepository = userRepository;
    }

    /**
     * 匿名プロファイル取得または作成
     */
    @PostMapping("/get-or-create")
    public ResponseEntity<AnonymousProfile> getOrCreateProfile(
            @RequestParam Long organizationId,
            @RequestParam Long userId) {
        log.info("Getting or creating anonymous profile for user {} in organization {}", userId, organizationId);
        Organization organization = organizationService.getOrganizationById(organizationId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
        AnonymousProfile profile = anonymousProfileService.getOrCreateAnonymousProfile(organization, user);
        return ResponseEntity.ok(profile);
    }

    /**
     * 匿名プロファイルIDで取得
     */
    @GetMapping("/{profileId}")
    public ResponseEntity<AnonymousProfile> getProfile(@PathVariable Long profileId) {
        log.info("Fetching anonymous profile: {}", profileId);
        AnonymousProfile profile = anonymousProfileService.getProfileById(profileId);
        return ResponseEntity.ok(profile);
    }

    /**
     * 組織×ユーザーで匿名プロファイル取得
     */
    @GetMapping("/organization/{organizationId}/user/{userId}")
    public ResponseEntity<AnonymousProfile> getProfileByOrganizationAndUser(
            @PathVariable Long organizationId,
            @PathVariable Long userId) {
        log.info("Fetching anonymous profile for user {} in organization {}", userId, organizationId);
        Organization organization = organizationService.getOrganizationById(organizationId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
        Optional<AnonymousProfile> profile = anonymousProfileService.getProfileByOrganizationAndUser(organization, user);
        return profile.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * ユーザーの全匿名プロファイル取得
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AnonymousProfile>> getProfilesByUser(@PathVariable Long userId) {
        log.info("Fetching all anonymous profiles for user: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
        List<AnonymousProfile> profiles = anonymousProfileService.getProfilesByUser(user);
        return ResponseEntity.ok(profiles);
    }

    /**
     * 組織内の全匿名プロファイル取得
     */
    @GetMapping("/organization/{organizationId}")
    public ResponseEntity<List<AnonymousProfile>> getProfilesByOrganization(@PathVariable Long organizationId) {
        log.info("Fetching all anonymous profiles for organization: {}", organizationId);
        Organization organization = organizationService.getOrganizationById(organizationId);
        List<AnonymousProfile> profiles = anonymousProfileService.getProfilesByOrganization(organization);
        return ResponseEntity.ok(profiles);
    }

    /**
     * 匿名IDからプロファイル検索
     */
    @GetMapping("/tenant/{tenantId}/by-anonymous-id/{anonymousId}")
    public ResponseEntity<AnonymousProfile> getProfileByAnonymousId(
            @PathVariable Long tenantId,
            @PathVariable String anonymousId) {
        log.info("Fetching anonymous profile by anonymousId: {} for tenant: {}", anonymousId, tenantId);
        Tenant tenant = tenantService.getTenantById(tenantId);
        Optional<AnonymousProfile> profile = anonymousProfileService.getProfileByAnonymousId(tenant, anonymousId);
        return profile.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 匿名プロファイル更新
     */
    @PutMapping("/{profileId}")
    public ResponseEntity<AnonymousProfile> updateProfile(
            @PathVariable Long profileId,
            @RequestBody UpdateProfileRequest request) {
        log.info("Updating anonymous profile: {}", profileId);
        AnonymousProfile updated = anonymousProfileService.updateProfile(
                profileId,
                request.getDisplayName(),
                request.getAvatarUrl());
        return ResponseEntity.ok(updated);
    }

    /**
     * 匿名プロファイル削除
     */
    @DeleteMapping("/{profileId}")
    public ResponseEntity<Void> deleteProfile(@PathVariable Long profileId) {
        log.info("Deleting anonymous profile: {}", profileId);
        anonymousProfileService.deleteProfile(profileId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 匿名プロファイルの存在確認
     */
    @GetMapping("/organization/{organizationId}/user/{userId}/exists")
    public ResponseEntity<Boolean> existsProfile(
            @PathVariable Long organizationId,
            @PathVariable Long userId) {
        log.info("Checking if anonymous profile exists for user {} in organization {}", userId, organizationId);
        Organization organization = organizationService.getOrganizationById(organizationId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
        boolean exists = anonymousProfileService.existsProfile(organization, user);
        return ResponseEntity.ok(exists);
    }

    /**
     * ユーザーの匿名プロファイル数カウント
     */
    @GetMapping("/user/{userId}/count")
    public ResponseEntity<Long> countProfilesByUser(@PathVariable Long userId) {
        log.info("Counting anonymous profiles for user: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
        long count = anonymousProfileService.countProfilesByUser(user);
        return ResponseEntity.ok(count);
    }

    // DTO

    public static class UpdateProfileRequest {
        private String displayName;
        private String avatarUrl;

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }

        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }
    }
}
