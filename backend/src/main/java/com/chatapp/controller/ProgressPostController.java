package com.chatapp.controller;

import com.chatapp.dto.CreateProgressPostRequest;
import com.chatapp.dto.ProgressPostResponse;
import com.chatapp.model.BoxType;
import com.chatapp.model.ProgressPost;
import com.chatapp.model.Organization;
import com.chatapp.model.Tenant;
import com.chatapp.model.User;
import com.chatapp.model.enums.PostType;
import com.chatapp.security.Permission;
import com.chatapp.security.RequirePermission;
import com.chatapp.service.ProgressPostService;
import com.chatapp.service.ProgressPostVisibilityService;
import com.chatapp.service.OrganizationService;
import com.chatapp.service.TenantService;
import com.chatapp.repository.UserRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

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
    private final ProgressPostVisibilityService visibilityService;

    public ProgressPostController(
            ProgressPostService progressPostService,
            OrganizationService organizationService,
            TenantService tenantService,
            UserRepository userRepository,
            ProgressPostVisibilityService visibilityService) {
        this.progressPostService = progressPostService;
        this.organizationService = organizationService;
        this.tenantService = tenantService;
        this.userRepository = userRepository;
        this.visibilityService = visibilityService;
    }

    /**
     * 進捗投稿を作成
     * 権限: POST_CREATE
     */
    @PostMapping
    @RequirePermission(Permission.POST_CREATE)
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
     * 権限チェック: 投稿を閲覧できるユーザーのみ
     */
    @GetMapping("/{postId}")
    public ResponseEntity<ProgressPost> getPost(
            @PathVariable Long postId,
            @RequestParam Long userId) {
        log.info("Fetching progress post: {} by user: {}", postId, userId);
        ProgressPost post = progressPostService.getPostById(postId);

        // 権限チェック
        if (!progressPostService.canViewPost(userId, post)) {
            log.warn("User {} does not have permission to view post {}", userId, postId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(post);
    }

    /**
     * テナントのタイムライン取得
     */
    @GetMapping("/tenant/{tenantId}/timeline")
    public ResponseEntity<Page<ProgressPostResponse>> getTenantTimeline(
            @PathVariable Long tenantId,
            Pageable pageable) {
        log.info("Fetching timeline for tenant: {}", tenantId);

        // テナントの存在チェック
        if (!tenantService.existsById(tenantId)) {
            log.warn("Tenant not found with ID: {} - returning empty timeline", tenantId);
            return ResponseEntity.ok(Page.empty(pageable));
        }

        Tenant tenant = tenantService.getTenantById(tenantId);
        Page<ProgressPost> posts = progressPostService.getTimelineByTenant(tenant, pageable);
        Page<ProgressPostResponse> responsePosts = progressPostService.toResponsePage(posts);
        return ResponseEntity.ok(responsePosts);
    }

    /**
     * 組織のタイムライン取得
     */
    @GetMapping("/organization/{organizationId}/timeline")
    public ResponseEntity<Page<ProgressPostResponse>> getOrganizationTimeline(
            @PathVariable Long organizationId,
            Pageable pageable) {
        log.info("Fetching timeline for organization: {}", organizationId);

        // 組織の存在チェック
        if (!organizationService.existsById(organizationId)) {
            log.warn("Organization not found with ID: {} - returning empty timeline", organizationId);
            return ResponseEntity.ok(Page.empty(pageable));
        }

        Organization organization = organizationService.getOrganizationById(organizationId);
        Page<ProgressPost> posts = progressPostService.getTimelineByOrganization(organization, pageable);
        Page<ProgressPostResponse> responsePosts = progressPostService.toResponsePage(posts);
        return ResponseEntity.ok(responsePosts);
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
     * Note: Visibilityフィールドは現在ProgressPostには実装されていないため、
     * このエンドポイントは組織の投稿をユーザー権限でフィルタリングして返します
     */
    @GetMapping("/organization/{organizationId}/by-visibility")
    public ResponseEntity<Page<ProgressPostResponse>> getPostsByVisibility(
            @PathVariable Long organizationId,
            @RequestParam Long userId,
            Pageable pageable) {
        log.info("Fetching posts for organization {} by user {}", organizationId, userId);

        // 組織を取得
        Organization organization = organizationService.getOrganizationById(organizationId);

        // 組織の全投稿を取得
        Page<ProgressPost> allPosts = progressPostService.getTimelineByOrganization(organization, pageable);

        // ユーザー権限でフィルタリング
        List<ProgressPost> filteredPosts = allPosts.getContent().stream()
                .filter(post -> progressPostService.canViewPost(userId, post))
                .collect(Collectors.toList());

        // DTOに変換
        List<ProgressPostResponse> responsePosts = progressPostService.toResponseList(filteredPosts);

        return ResponseEntity.ok(new PageImpl<>(responsePosts, pageable, responsePosts.size()));
    }

    /**
     * 進捗投稿を更新
     * 権限: POST_UPDATE（自分の投稿）または POST_MODERATE（モデレーター）
     */
    @PutMapping("/{postId}")
    @RequirePermission(value = {Permission.POST_UPDATE, Permission.POST_MODERATE}, requireAll = false)
    public ResponseEntity<ProgressPost> updatePost(
            @PathVariable Long postId,
            @RequestBody ProgressPost updateData) {
        log.info("Updating progress post: {}", postId);
        ProgressPost updated = progressPostService.updatePost(postId, updateData);
        return ResponseEntity.ok(updated);
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
     * 権限: POST_DELETE（自分の投稿）または POST_MODERATE（モデレーター）
     */
    @DeleteMapping("/{postId}")
    @RequirePermission(value = {Permission.POST_DELETE, Permission.POST_MODERATE}, requireAll = false)
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

    // ========================================
    // Box System関連エンドポイント
    // ========================================

    /**
     * BoxTypeで進捗投稿を取得
     */
    @GetMapping("/box/{boxTypeId}")
    public ResponseEntity<Page<ProgressPost>> getPostsByBoxType(
            @PathVariable Long boxTypeId,
            Pageable pageable) {
        log.info("Fetching posts for box type: {}", boxTypeId);
        BoxType boxType = progressPostService.getDefaultBoxType(boxTypeId, null);
        Page<ProgressPost> posts = progressPostService.getPostsByBoxType(boxType, pageable);
        return ResponseEntity.ok(posts);
    }

    /**
     * テナントとBoxTypeで進捗投稿を取得
     */
    @GetMapping("/tenant/{tenantId}/box/{boxTypeName}")
    public ResponseEntity<Page<ProgressPost>> getPostsByTenantAndBoxType(
            @PathVariable Long tenantId,
            @PathVariable String boxTypeName,
            Pageable pageable) {
        log.info("Fetching posts for tenant {} and box type {}", tenantId, boxTypeName);
        Tenant tenant = tenantService.getTenantById(tenantId);
        BoxType boxType = progressPostService.getDefaultBoxType(tenantId, boxTypeName);
        Page<ProgressPost> posts = progressPostService.getPostsByTenantAndBoxType(tenant, boxType, pageable);
        return ResponseEntity.ok(posts);
    }

    /**
     * 複数のBoxTypeで進捗投稿を取得（階層的閲覧）
     */
    @GetMapping("/tenant/{tenantId}/boxes")
    public ResponseEntity<Page<ProgressPost>> getPostsByTenantAndBoxTypes(
            @PathVariable Long tenantId,
            @RequestParam List<String> boxTypeNames,
            Pageable pageable) {
        log.info("Fetching posts for tenant {} with box types {}", tenantId, boxTypeNames);
        Tenant tenant = tenantService.getTenantById(tenantId);

        List<BoxType> boxTypes = boxTypeNames.stream()
                .map(name -> progressPostService.getDefaultBoxType(tenantId, name))
                .collect(Collectors.toList());

        Page<ProgressPost> posts = progressPostService.getPostsByTenantAndBoxTypes(tenant, boxTypes, pageable);
        return ResponseEntity.ok(posts);
    }

    /**
     * 組織とBoxTypeで進捗投稿を取得
     */
    @GetMapping("/organization/{organizationId}/box/{boxTypeName}")
    public ResponseEntity<Page<ProgressPost>> getPostsByOrganizationAndBoxType(
            @PathVariable Long organizationId,
            @PathVariable String boxTypeName,
            Pageable pageable) {
        log.info("Fetching posts for organization {} and box type {}", organizationId, boxTypeName);
        Organization organization = organizationService.getOrganizationById(organizationId);

        Long tenantId = organization.getTenant().getId();
        BoxType boxType = progressPostService.getDefaultBoxType(tenantId, boxTypeName);

        Page<ProgressPost> posts = progressPostService.getPostsByOrganizationAndBoxType(
                organization, boxType, pageable);
        return ResponseEntity.ok(posts);
    }

    /**
     * テナント、BoxType、投稿タイプで進捗投稿を取得
     */
    @GetMapping("/tenant/{tenantId}/box/{boxTypeName}/type/{postType}")
    public ResponseEntity<Page<ProgressPost>> getPostsByTenantAndBoxTypeAndType(
            @PathVariable Long tenantId,
            @PathVariable String boxTypeName,
            @PathVariable PostType postType,
            Pageable pageable) {
        log.info("Fetching posts of type {} for tenant {} and box type {}", postType, tenantId, boxTypeName);
        Tenant tenant = tenantService.getTenantById(tenantId);
        BoxType boxType = progressPostService.getDefaultBoxType(tenantId, boxTypeName);

        Page<ProgressPost> posts = progressPostService.getPostsByTenantAndBoxTypeAndType(
                tenant, boxType, postType, pageable);
        return ResponseEntity.ok(posts);
    }

    /**
     * 日付範囲とBoxTypeで進捗投稿を取得
     */
    @GetMapping("/tenant/{tenantId}/box/{boxTypeName}/by-date")
    public ResponseEntity<List<ProgressPost>> getPostsByBoxTypeAndDateRange(
            @PathVariable Long tenantId,
            @PathVariable String boxTypeName,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        log.info("Fetching posts for box type {} between {} and {}", boxTypeName, startDate, endDate);
        Tenant tenant = tenantService.getTenantById(tenantId);
        BoxType boxType = progressPostService.getDefaultBoxType(tenantId, boxTypeName);

        List<ProgressPost> posts = progressPostService.getPostsByBoxTypeAndDateRange(
                tenant, boxType, startDate, endDate);
        return ResponseEntity.ok(posts);
    }

    /**
     * 返信投稿を取得
     * 権限チェック: 親投稿を閲覧できるユーザーのみ返信も閲覧可能
     */
    @GetMapping("/{parentPostId}/replies")
    public ResponseEntity<?> getRepliesByParentPostId(
            @PathVariable Long parentPostId,
            @RequestParam Long userId) {
        log.info("Fetching replies for parent post: {} by user: {}", parentPostId, userId);

        // 親投稿を取得
        ProgressPost parentPost = progressPostService.getPostById(parentPostId);

        // 親投稿の閲覧権限チェック
        if (!progressPostService.canViewPost(userId, parentPost)) {
            log.warn("User {} does not have permission to view replies for post {}", userId, parentPostId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You do not have permission to view replies for this post");
        }

        List<ProgressPost> replies = progressPostService.getRepliesByParentPostId(parentPostId);
        return ResponseEntity.ok(replies);
    }

    /**
     * 返信投稿を取得（ページネーション）
     */
    @GetMapping("/{parentPostId}/replies/paged")
    public ResponseEntity<Page<ProgressPost>> getRepliesByParentPostPaged(
            @PathVariable Long parentPostId,
            Pageable pageable) {
        log.info("Fetching paged replies for parent post: {}", parentPostId);
        ProgressPost parentPost = progressPostService.getPostById(parentPostId);
        Page<ProgressPost> replies = progressPostService.getRepliesByParentPost(parentPost, pageable);
        return ResponseEntity.ok(replies);
    }

    /**
     * ルート投稿のみを取得（返信ではない投稿）
     */
    @GetMapping("/tenant/{tenantId}/root-posts")
    public ResponseEntity<Page<ProgressPost>> getRootPostsByTenant(
            @PathVariable Long tenantId,
            Pageable pageable) {
        log.info("Fetching root posts for tenant: {}", tenantId);
        Tenant tenant = tenantService.getTenantById(tenantId);
        Page<ProgressPost> posts = progressPostService.getRootPostsByTenant(tenant, pageable);
        return ResponseEntity.ok(posts);
    }

    /**
     * BoxTypeでルート投稿のみを取得
     */
    @GetMapping("/tenant/{tenantId}/box/{boxTypeName}/root-posts")
    public ResponseEntity<Page<ProgressPost>> getRootPostsByTenantAndBoxType(
            @PathVariable Long tenantId,
            @PathVariable String boxTypeName,
            Pageable pageable) {
        log.info("Fetching root posts for tenant {} and box type {}", tenantId, boxTypeName);
        Tenant tenant = tenantService.getTenantById(tenantId);
        BoxType boxType = progressPostService.getDefaultBoxType(tenantId, boxTypeName);

        Page<ProgressPost> posts = progressPostService.getRootPostsByTenantAndBoxType(
                tenant, boxType, pageable);
        return ResponseEntity.ok(posts);
    }

    /**
     * 返信投稿を作成
     * 権限: 投稿を閲覧でき、かつ返信権限があるユーザーのみ
     */
    @PostMapping("/{parentPostId}/replies")
    @RequirePermission(Permission.POST_CREATE)
    public ResponseEntity<?> createReply(
            @PathVariable Long parentPostId,
            @RequestBody ProgressPost replyPost) {
        log.info("Creating reply for parent post: {} by user: {}", parentPostId, replyPost.getAuthor().getId());

        // 親投稿を取得
        ProgressPost parentPost = progressPostService.getPostById(parentPostId);

        // 返信権限チェック
        Long userId = replyPost.getAuthor().getId();
        if (!progressPostService.canReplyToPost(userId, parentPost)) {
            log.warn("User {} does not have permission to reply to post {}", userId, parentPostId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You do not have permission to reply to this post");
        }

        ProgressPost savedReply = progressPostService.createReply(parentPost, replyPost);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedReply);
    }

    /**
     * BoxTypeの投稿数をカウント
     */
    @GetMapping("/box/{boxTypeId}/count")
    public ResponseEntity<Long> countPostsByBoxType(@PathVariable Long boxTypeId) {
        log.info("Counting posts for box type: {}", boxTypeId);
        BoxType boxType = progressPostService.getDefaultBoxType(boxTypeId, null);
        long count = progressPostService.countPostsByBoxType(boxType);
        return ResponseEntity.ok(count);
    }

    /**
     * テナントとBoxTypeで投稿数をカウント
     */
    @GetMapping("/tenant/{tenantId}/box/{boxTypeName}/count")
    public ResponseEntity<Long> countPostsByTenantAndBoxType(
            @PathVariable Long tenantId,
            @PathVariable String boxTypeName) {
        log.info("Counting posts for tenant {} and box type {}", tenantId, boxTypeName);
        Tenant tenant = tenantService.getTenantById(tenantId);
        BoxType boxType = progressPostService.getDefaultBoxType(tenantId, boxTypeName);
        long count = progressPostService.countPostsByTenantAndBoxType(tenant, boxType);
        return ResponseEntity.ok(count);
    }

    /**
     * テナントのアクティブなBox種別を全て取得
     */
    @GetMapping("/tenant/{tenantId}/box-types")
    public ResponseEntity<List<BoxType>> getActiveBoxTypes(@PathVariable Long tenantId) {
        log.info("Fetching active box types for tenant: {}", tenantId);
        List<BoxType> boxTypes = progressPostService.getActiveBoxTypes(tenantId);
        return ResponseEntity.ok(boxTypes);
    }

    // ========================================
    // 組織階層による投稿フィルタリング
    // ========================================

    /**
     * 組織とその配下組織の投稿を取得（階層的に閲覧）
     * ユーザーが所属する組織またはその上位組織の投稿のみ表示
     *
     * @param organizationId 選択された組織ID
     * @param userId 閲覧するユーザーID
     * @param pageable ページネーション情報
     * @return 閲覧可能な投稿ページ
     */
    @GetMapping("/organization/{organizationId}/hierarchical")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<Page<ProgressPostResponse>> getOrganizationHierarchicalPosts(
            @PathVariable Long organizationId,
            @RequestParam Long userId,
            Pageable pageable) {
        log.info("Fetching hierarchical posts for organization {} by user {}", organizationId, userId);

        // ユーザーを取得
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // ユーザーがこの組織以下の投稿を閲覧できるかチェック
        if (!visibilityService.canViewOrganizationPosts(user, organizationId)) {
            log.warn("User {} does not have permission to view posts for organization {}", userId, organizationId);
            return ResponseEntity.ok(Page.empty(pageable));
        }

        // 組織を取得
        Organization organization = organizationService.getOrganizationById(organizationId);

        // 組織のタイムラインを取得
        Page<ProgressPost> posts = progressPostService.getTimelineByOrganization(organization, pageable);

        // ユーザーが閲覧可能な投稿のみフィルタリング
        List<ProgressPost> filteredPosts = visibilityService.filterViewablePosts(user, posts.getContent());

        // DTOに変換
        List<ProgressPostResponse> responsePosts = progressPostService.toResponseList(filteredPosts);

        // フィルタリング後のページを返す
        return ResponseEntity.ok(new org.springframework.data.domain.PageImpl<>(
                responsePosts,
                pageable,
                responsePosts.size()
        ));
    }

    /**
     * ユーザーが閲覧可能な全組織の投稿を取得
     * ユーザーが所属する組織とその下位組織の投稿を表示
     *
     * @param userId ユーザーID
     * @param tenantId テナントID
     * @param pageable ページネーション情報
     * @return 閲覧可能な投稿ページ
     */
    @GetMapping("/user/{userId}/viewable")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<Page<ProgressPostResponse>> getViewablePostsForUser(
            @PathVariable Long userId,
            @RequestParam Long tenantId,
            Pageable pageable) {
        log.info("Fetching viewable posts for user {} in tenant {}", userId, tenantId);

        // ユーザーを取得
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // テナントを取得
        Tenant tenant = tenantService.getTenantById(tenantId);

        // テナントの全投稿を取得
        Page<ProgressPost> allPosts = progressPostService.getTimelineByTenant(tenant, pageable);

        // ユーザーが閲覧可能な投稿のみフィルタリング
        List<ProgressPost> filteredPosts = visibilityService.filterViewablePosts(user, allPosts.getContent());

        // DTOに変換
        List<ProgressPostResponse> responsePosts = progressPostService.toResponseList(filteredPosts);

        // フィルタリング後のページを返す
        return ResponseEntity.ok(new org.springframework.data.domain.PageImpl<>(
                responsePosts,
                pageable,
                responsePosts.size()
        ));
    }
}
