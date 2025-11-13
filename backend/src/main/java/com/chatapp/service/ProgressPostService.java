package com.chatapp.service;

import com.chatapp.dto.CreateProgressPostRequest;
import com.chatapp.model.Organization;
import com.chatapp.model.ProgressPost;
import com.chatapp.model.Tenant;
import com.chatapp.model.User;
import com.chatapp.model.enums.PostType;
import com.chatapp.model.enums.Visibility;
import com.chatapp.repository.ProgressPostRepository;
import com.chatapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 進捗投稿サービス
 * 投稿の作成、更新、削除、タイムライン取得などのビジネスロジックを提供
 */
@Service
@Transactional(readOnly = true)
public class ProgressPostService {

    private static final Logger log = LoggerFactory.getLogger(ProgressPostService.class);
    private final ProgressPostRepository progressPostRepository;
    private final TenantService tenantService;
    private final OrganizationService organizationService;
    private final UserRepository userRepository;

    public ProgressPostService(
            ProgressPostRepository progressPostRepository,
            TenantService tenantService,
            OrganizationService organizationService,
            UserRepository userRepository) {
        this.progressPostRepository = progressPostRepository;
        this.tenantService = tenantService;
        this.organizationService = organizationService;
        this.userRepository = userRepository;
    }

    /**
     * 進捗投稿作成（DTOから）
     *
     * @param request 作成リクエスト
     * @return 作成された投稿
     */
    @Transactional
    public ProgressPost createPost(CreateProgressPostRequest request) {
        log.info("Creating new progress post from request - tenantId: {}, orgId: {}, authorId: {}",
                request.getTenantId(), request.getOrganizationId(), request.getAuthorId());

        // IDから実エンティティを取得
        Tenant tenant = tenantService.getTenantById(request.getTenantId());
        Organization organization = organizationService.getOrganizationById(request.getOrganizationId());

        // authorIdからユーザーを取得
        User author = userRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + request.getAuthorId()));

        // 投稿エンティティを作成
        ProgressPost post = new ProgressPost();
        post.setTenant(tenant);
        post.setOrganization(organization);
        post.setAuthor(author);
        post.setPostType(request.getPostType() != null ? request.getPostType() : PostType.PROGRESS);
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setAchievementRate(request.getAchievementRate());
        post.setBlockers(request.getBlockers());
        post.setLearnings(request.getLearnings());
        post.setNextAction(request.getNextAction());
        post.setVisibility(request.getVisibility() != null ? request.getVisibility() : Visibility.ORGANIZATION);
        post.setTags(request.getTags());

        // 投稿日をパース
        if (request.getPostDate() != null) {
            post.setPostDate(LocalDate.parse(request.getPostDate()));
        } else {
            post.setPostDate(LocalDate.now());
        }

        // ターゲット組織
        if (request.getTargetOrganizationId() != null) {
            Organization targetOrg = organizationService.getOrganizationById(request.getTargetOrganizationId());
            post.setTargetOrganization(targetOrg);
        }

        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());

        // カウンターの初期化
        post.setReactionCount(0);
        post.setCommentCount(0);
        post.setViewCount(0);

        ProgressPost savedPost = progressPostRepository.save(post);
        log.info("Progress post created successfully with ID: {}", savedPost.getId());

        return savedPost;
    }

    /**
     * 進捗投稿作成（エンティティから）
     *
     * @param post 作成する投稿
     * @return 作成された投稿
     */
    @Transactional
    public ProgressPost createPost(ProgressPost post) {
        log.info("Creating new progress post by author: {}", post.getAuthor().getId());

        // 投稿日が未設定の場合は今日の日付を設定
        if (post.getPostDate() == null) {
            post.setPostDate(LocalDate.now());
        }

        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());

        // カウンターの初期化
        if (post.getReactionCount() == null) {
            post.setReactionCount(0);
        }
        if (post.getCommentCount() == null) {
            post.setCommentCount(0);
        }
        if (post.getViewCount() == null) {
            post.setViewCount(0);
        }

        ProgressPost savedPost = progressPostRepository.save(post);
        log.info("Progress post created successfully with ID: {}", savedPost.getId());

        return savedPost;
    }

    /**
     * 投稿ID取得
     *
     * @param id 投稿ID
     * @return 投稿情報
     * @throws EntityNotFoundException 投稿が見つからない場合
     */
    public ProgressPost getPostById(Long id) {
        log.debug("Fetching progress post by ID: {}", id);
        return progressPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Progress post not found with ID: " + id));
    }

    /**
     * 投稿ID取得（テナントチェック付き）
     *
     * @param id 投稿ID
     * @param tenant テナント
     * @return 投稿情報
     * @throws EntityNotFoundException 投稿が見つからない場合
     */
    public ProgressPost getPostByIdAndTenant(Long id, Tenant tenant) {
        log.debug("Fetching progress post by ID: {} for tenant: {}", id, tenant.getName());
        return progressPostRepository.findByIdAndTenant(id, tenant)
                .orElseThrow(() -> new EntityNotFoundException("Progress post not found with ID: " + id));
    }

    /**
     * テナントのタイムライン取得
     *
     * @param tenant テナント
     * @param pageable ページネーション情報
     * @return 投稿ページ
     */
    public Page<ProgressPost> getTimelineByTenant(Tenant tenant, Pageable pageable) {
        log.debug("Fetching timeline for tenant: {}", tenant.getName());
        return progressPostRepository.findByTenantOrderByCreatedAtDesc(tenant, pageable);
    }

    /**
     * 組織のタイムライン取得
     *
     * @param organization 組織
     * @param pageable ページネーション情報
     * @return 投稿ページ
     */
    public Page<ProgressPost> getTimelineByOrganization(Organization organization, Pageable pageable) {
        log.debug("Fetching timeline for organization: {}", organization.getName());
        return progressPostRepository.findByOrganizationOrderByCreatedAtDesc(organization, pageable);
    }

    /**
     * 著者の投稿一覧取得
     *
     * @param author 著者
     * @param pageable ページネーション情報
     * @return 投稿ページ
     */
    public Page<ProgressPost> getPostsByAuthor(User author, Pageable pageable) {
        log.debug("Fetching posts by author: {}", author.getId());
        return progressPostRepository.findByAuthorOrderByCreatedAtDesc(author, pageable);
    }

    /**
     * 日付範囲で投稿取得
     *
     * @param tenant テナント
     * @param startDate 開始日
     * @param endDate 終了日
     * @return 投稿リスト
     */
    public List<ProgressPost> getPostsByDateRange(Tenant tenant, LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching posts for tenant {} between {} and {}", tenant.getName(), startDate, endDate);
        return progressPostRepository.findByTenantAndPostDateBetweenOrderByPostDateDesc(tenant, startDate, endDate);
    }

    /**
     * 組織×日付範囲で投稿取得
     *
     * @param organization 組織
     * @param startDate 開始日
     * @param endDate 終了日
     * @return 投稿リスト
     */
    public List<ProgressPost> getPostsByOrganizationAndDateRange(
            Organization organization, LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching posts for organization {} between {} and {}",
                  organization.getName(), startDate, endDate);
        return progressPostRepository.findByOrganizationAndPostDateBetweenOrderByPostDateDesc(
                organization, startDate, endDate);
    }

    /**
     * 投稿タイプでフィルタリング
     *
     * @param tenant テナント
     * @param postType 投稿タイプ
     * @param pageable ページネーション情報
     * @return 投稿ページ
     */
    public Page<ProgressPost> getPostsByType(Tenant tenant, PostType postType, Pageable pageable) {
        log.debug("Fetching posts of type {} for tenant {}", postType, tenant.getName());
        return progressPostRepository.findByTenantAndPostType(tenant, postType, pageable);
    }

    /**
     * 公開範囲でフィルタリング
     *
     * @param organization 組織
     * @param visibilities 公開範囲リスト
     * @param pageable ページネーション情報
     * @return 投稿ページ
     */
    public Page<ProgressPost> getPostsByVisibility(
            Organization organization, List<Visibility> visibilities, Pageable pageable) {
        log.debug("Fetching posts with visibilities {} for organization {}",
                  visibilities, organization.getName());
        return progressPostRepository.findByOrganizationAndVisibilityIn(organization, visibilities, pageable);
    }

    /**
     * 日付範囲×投稿タイプで取得
     *
     * @param tenant テナント
     * @param startDate 開始日
     * @param endDate 終了日
     * @param postType 投稿タイプ
     * @return 投稿リスト
     */
    public List<ProgressPost> getPostsByDateRangeAndType(
            Tenant tenant, LocalDate startDate, LocalDate endDate, PostType postType) {
        log.debug("Fetching posts of type {} for tenant {} between {} and {}",
                  postType, tenant.getName(), startDate, endDate);
        return progressPostRepository.findByTenantAndDateRangeAndPostType(tenant, startDate, endDate, postType);
    }

    /**
     * 投稿更新
     *
     * @param id 投稿ID
     * @param updateData 更新データ
     * @return 更新された投稿
     * @throws EntityNotFoundException 投稿が見つからない場合
     */
    @Transactional
    public ProgressPost updatePost(Long id, ProgressPost updateData) {
        log.info("Updating progress post ID: {}", id);

        ProgressPost existingPost = getPostById(id);

        // 更新可能なフィールドのみ更新
        if (updateData.getTitle() != null) {
            existingPost.setTitle(updateData.getTitle());
        }
        if (updateData.getContent() != null) {
            existingPost.setContent(updateData.getContent());
        }
        if (updateData.getAchievementRate() != null) {
            existingPost.setAchievementRate(updateData.getAchievementRate());
        }
        if (updateData.getBlockers() != null) {
            existingPost.setBlockers(updateData.getBlockers());
        }
        if (updateData.getLearnings() != null) {
            existingPost.setLearnings(updateData.getLearnings());
        }
        if (updateData.getNextAction() != null) {
            existingPost.setNextAction(updateData.getNextAction());
        }
        if (updateData.getTags() != null) {
            existingPost.setTags(updateData.getTags());
        }

        existingPost.setUpdatedAt(LocalDateTime.now());

        ProgressPost updatedPost = progressPostRepository.save(existingPost);
        log.info("Progress post updated successfully: {}", id);

        return updatedPost;
    }

    /**
     * リアクションカウント増加
     *
     * @param postId 投稿ID
     */
    @Transactional
    public void incrementReactionCount(Long postId) {
        ProgressPost post = getPostById(postId);
        post.setReactionCount(post.getReactionCount() + 1);
        progressPostRepository.save(post);
    }

    /**
     * リアクションカウント減少
     *
     * @param postId 投稿ID
     */
    @Transactional
    public void decrementReactionCount(Long postId) {
        ProgressPost post = getPostById(postId);
        if (post.getReactionCount() > 0) {
            post.setReactionCount(post.getReactionCount() - 1);
            progressPostRepository.save(post);
        }
    }

    /**
     * コメントカウント増加
     *
     * @param postId 投稿ID
     */
    @Transactional
    public void incrementCommentCount(Long postId) {
        ProgressPost post = getPostById(postId);
        post.setCommentCount(post.getCommentCount() + 1);
        progressPostRepository.save(post);
    }

    /**
     * 閲覧カウント増加
     *
     * @param postId 投稿ID
     */
    @Transactional
    public void incrementViewCount(Long postId) {
        ProgressPost post = getPostById(postId);
        post.setViewCount(post.getViewCount() + 1);
        progressPostRepository.save(post);
    }

    /**
     * 投稿削除
     *
     * @param id 投稿ID
     */
    @Transactional
    public void deletePost(Long id) {
        log.info("Deleting progress post ID: {}", id);

        if (!progressPostRepository.existsById(id)) {
            throw new EntityNotFoundException("Progress post not found with ID: " + id);
        }

        progressPostRepository.deleteById(id);
        log.info("Progress post deleted successfully: {}", id);
    }

    /**
     * 著者の投稿数カウント
     *
     * @param author 著者
     * @return 投稿数
     */
    public long countPostsByAuthor(User author) {
        return progressPostRepository.countByAuthor(author);
    }

    /**
     * 組織×期間の投稿数カウント
     *
     * @param organization 組織
     * @param startDate 開始日
     * @param endDate 終了日
     * @return 投稿数
     */
    public long countPostsByOrganizationAndDateRange(
            Organization organization, LocalDate startDate, LocalDate endDate) {
        return progressPostRepository.countByOrganizationAndPostDateBetween(organization, startDate, endDate);
    }
}
