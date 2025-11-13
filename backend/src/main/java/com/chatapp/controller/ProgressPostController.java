package com.chatapp.controller;

import com.chatapp.dto.CreateProgressPostRequest;
import com.chatapp.model.ProgressPost;
import com.chatapp.model.Organization;
import com.chatapp.model.Tenant;
import com.chatapp.model.User;
import com.chatapp.model.enums.PostType;
import com.chatapp.model.enums.Visibility;
import com.chatapp.service.ProgressPostService;
import com.chatapp.service.OrganizationService;
import com.chatapp.service.TenantService;
import com.chatapp.repository.UserRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 進捗投稿のREST APIエンドポイント
 */
@RestController
@RequestMapping("/api/progress-posts")
public class ProgressPostController {

    private static final Logger log = LoggerFactory.getLogger(ProgressPostController.class);

    private final ProgressPostService progressPostService;
    private final OrganizationService organizationService;
    private final TenantService tenantService;
    private final UserRepository userRepository;

    public ProgressPostController(
            ProgressPostService progressPostService,
            OrganizationService organizationService,
            TenantService tenantService,
            UserRepository userRepository) {
        this.progressPostService = progressPostService;
        this.organizationService = organizationService;
        this.tenantService = tenantService;
        this.userRepository = userRepository;
    }

    /**
     * 進捗投稿を作成
     */
    @PostMapping
    public ResponseEntity<ProgressPost> createPost(@Valid @RequestBody CreateProgressPostRequest request) {
        log.info("Creating progress post - tenantId: {}, orgId: {}, authorId: {}",
                request.getTenantId(), request.getOrganizationId(), request.getAuthorId());
        try {
            ProgressPost created = progressPostService.createPost(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            log.error("Error creating progress post", e);
            throw e;
        }
    }

    /**
     * 進捗投稿をIDで取得
     */
    @GetMapping("/{postId}")
    public ResponseEntity<ProgressPost> getPost(@PathVariable Long postId) {
        log.info("Fetching progress post: {}", postId);
        ProgressPost post = progressPostService.getPostById(postId);
        return ResponseEntity.ok(post);
    }

    /**
     * テナントのタイムライン取得
     */
    @GetMapping("/tenant/{tenantId}/timeline")
    public ResponseEntity<Page<ProgressPost>> getTenantTimeline(
            @PathVariable Long tenantId,
            Pageable pageable) {
        log.info("Fetching timeline for tenant: {}", tenantId);
        Tenant tenant = tenantService.getTenantById(tenantId);
        Page<ProgressPost> posts = progressPostService.getTimelineByTenant(tenant, pageable);
        return ResponseEntity.ok(posts);
    }

    /**
     * 組織のタイムライン取得
     */
    @GetMapping("/organization/{organizationId}/timeline")
    public ResponseEntity<Page<ProgressPost>> getOrganizationTimeline(
            @PathVariable Long organizationId,
            Pageable pageable) {
        log.info("Fetching timeline for organization: {}", organizationId);
        Organization organization = organizationService.getOrganizationById(organizationId);
        Page<ProgressPost> posts = progressPostService.getTimelineByOrganization(organization, pageable);
        return ResponseEntity.ok(posts);
    }

    /**
     * 著者の投稿一覧取得
     */
    @GetMapping("/author/{authorId}")
    public ResponseEntity<Page<ProgressPost>> getPostsByAuthor(
            @PathVariable Long authorId,
            Pageable pageable) {
        log.info("Fetching posts by author: {}", authorId);
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + authorId));
        Page<ProgressPost> posts = progressPostService.getPostsByAuthor(author, pageable);
        return ResponseEntity.ok(posts);
    }

    /**
     * 日付範囲で投稿取得
     */
    @GetMapping("/tenant/{tenantId}/by-date")
    public ResponseEntity<List<ProgressPost>> getPostsByDateRange(
            @PathVariable Long tenantId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        log.info("Fetching posts for tenant {} between {} and {}", tenantId, startDate, endDate);
        Tenant tenant = tenantService.getTenantById(tenantId);
        List<ProgressPost> posts = progressPostService.getPostsByDateRange(tenant, startDate, endDate);
        return ResponseEntity.ok(posts);
    }

    /**
     * 組織×日付範囲で投稿取得
     */
    @GetMapping("/organization/{organizationId}/by-date")
    public ResponseEntity<List<ProgressPost>> getPostsByOrganizationAndDateRange(
            @PathVariable Long organizationId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        log.info("Fetching posts for organization {} between {} and {}", organizationId, startDate, endDate);
        Organization organization = organizationService.getOrganizationById(organizationId);
        List<ProgressPost> posts = progressPostService.getPostsByOrganizationAndDateRange(
                organization, startDate, endDate);
        return ResponseEntity.ok(posts);
    }

    /**
     * 投稿タイプでフィルタリング
     */
    @GetMapping("/tenant/{tenantId}/by-type")
    public ResponseEntity<Page<ProgressPost>> getPostsByType(
            @PathVariable Long tenantId,
            @RequestParam PostType postType,
            Pageable pageable) {
        log.info("Fetching posts of type {} for tenant {}", postType, tenantId);
        Tenant tenant = tenantService.getTenantById(tenantId);
        Page<ProgressPost> posts = progressPostService.getPostsByType(tenant, postType, pageable);
        return ResponseEntity.ok(posts);
    }

    /**
     * 公開範囲でフィルタリング
     */
    @GetMapping("/organization/{organizationId}/by-visibility")
    public ResponseEntity<Page<ProgressPost>> getPostsByVisibility(
            @PathVariable Long organizationId,
            @RequestParam List<Visibility> visibilities,
            Pageable pageable) {
        log.info("Fetching posts with visibilities {} for organization {}", visibilities, organizationId);
        Organization organization = organizationService.getOrganizationById(organizationId);
        Page<ProgressPost> posts = progressPostService.getPostsByVisibility(organization, visibilities, pageable);
        return ResponseEntity.ok(posts);
    }

    /**
     * 進捗投稿を更新
     */
    @PutMapping("/{postId}")
    public ResponseEntity<ProgressPost> updatePost(
            @PathVariable Long postId,
            @RequestBody ProgressPost updateData) {
        log.info("Updating progress post: {}", postId);
        ProgressPost updated = progressPostService.updatePost(postId, updateData);
        return ResponseEntity.ok(updated);
    }

    /**
     * リアクションカウント増加
     */
    @PostMapping("/{postId}/reactions/increment")
    public ResponseEntity<Void> incrementReactionCount(@PathVariable Long postId) {
        log.info("Incrementing reaction count for post: {}", postId);
        progressPostService.incrementReactionCount(postId);
        return ResponseEntity.ok().build();
    }

    /**
     * リアクションカウント減少
     */
    @PostMapping("/{postId}/reactions/decrement")
    public ResponseEntity<Void> decrementReactionCount(@PathVariable Long postId) {
        log.info("Decrementing reaction count for post: {}", postId);
        progressPostService.decrementReactionCount(postId);
        return ResponseEntity.ok().build();
    }

    /**
     * コメントカウント増加
     */
    @PostMapping("/{postId}/comments/increment")
    public ResponseEntity<Void> incrementCommentCount(@PathVariable Long postId) {
        log.info("Incrementing comment count for post: {}", postId);
        progressPostService.incrementCommentCount(postId);
        return ResponseEntity.ok().build();
    }

    /**
     * 閲覧カウント増加
     */
    @PostMapping("/{postId}/views/increment")
    public ResponseEntity<Void> incrementViewCount(@PathVariable Long postId) {
        log.info("Incrementing view count for post: {}", postId);
        progressPostService.incrementViewCount(postId);
        return ResponseEntity.ok().build();
    }

    /**
     * 進捗投稿を削除
     */
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId) {
        log.info("Deleting progress post: {}", postId);
        progressPostService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 著者の投稿数カウント
     */
    @GetMapping("/author/{authorId}/count")
    public ResponseEntity<Long> countPostsByAuthor(@PathVariable Long authorId) {
        log.info("Counting posts by author: {}", authorId);
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + authorId));
        long count = progressPostService.countPostsByAuthor(author);
        return ResponseEntity.ok(count);
    }

    /**
     * 組織×期間の投稿数カウント
     */
    @GetMapping("/organization/{organizationId}/count")
    public ResponseEntity<Long> countPostsByOrganizationAndDateRange(
            @PathVariable Long organizationId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        log.info("Counting posts for organization {} between {} and {}", organizationId, startDate, endDate);
        Organization organization = organizationService.getOrganizationById(organizationId);
        long count = progressPostService.countPostsByOrganizationAndDateRange(organization, startDate, endDate);
        return ResponseEntity.ok(count);
    }
}
