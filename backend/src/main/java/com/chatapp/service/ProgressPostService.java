package com.chatapp.service;

import com.chatapp.dto.CreateProgressPostRequest;
import com.chatapp.dto.ProgressPostResponse;
import com.chatapp.dto.ProgressPostResponse.OrganizationInfo;
import com.chatapp.model.BoxType;
import com.chatapp.model.Organization;
import com.chatapp.model.OrganizationMember;
import com.chatapp.model.ProgressPost;
import com.chatapp.model.Tenant;
import com.chatapp.model.User;
import com.chatapp.model.enums.PostType;
import com.chatapp.repository.BoxTypeRepository;
import com.chatapp.repository.OrganizationMemberRepository;
import com.chatapp.repository.ProgressPostRepository;
import com.chatapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
    private final BoxTypeRepository boxTypeRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final BoxPermissionService boxPermissionService;

    public ProgressPostService(
            ProgressPostRepository progressPostRepository,
            TenantService tenantService,
            OrganizationService organizationService,
            UserRepository userRepository,
            BoxTypeRepository boxTypeRepository,
            OrganizationMemberRepository organizationMemberRepository,
            BoxPermissionService boxPermissionService) {
        this.progressPostRepository = progressPostRepository;
        this.tenantService = tenantService;
        this.organizationService = organizationService;
        this.userRepository = userRepository;
        this.boxTypeRepository = boxTypeRepository;
        this.organizationMemberRepository = organizationMemberRepository;
        this.boxPermissionService = boxPermissionService;
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
        post.setCommentCount(0);
        post.setViewCount(0);

        // 匿名番号の割り当て（組織内で連番）
        Integer maxAnonymousNumber = progressPostRepository
                .findMaxAnonymousNumberByOrganizationId(organization.getId());
        post.setAnonymousNumber(maxAnonymousNumber + 1);

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
        if (post.getCommentCount() == null) {
            post.setCommentCount(0);
        }
        if (post.getViewCount() == null) {
            post.setViewCount(0);
        }

        // 匿名番号の割り当て（組織内で連番）
        if (post.getAnonymousNumber() == null) {
            Integer maxAnonymousNumber = progressPostRepository
                    .findMaxAnonymousNumberByOrganizationId(post.getOrganization().getId());
            post.setAnonymousNumber(maxAnonymousNumber + 1);
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
     * ユーザーが投稿を閲覧可能かチェック
     *
     * @param userId ユーザーID
     * @param post 投稿
     * @return 閲覧可能な場合true
     */
    public boolean canViewPost(Long userId, ProgressPost post) {
        if (post == null) {
            return false;
        }

        // 投稿者本人は常に閲覧可能
        if (post.getAuthor() != null && post.getAuthor().getId().equals(userId)) {
            return true;
        }

        // まず投稿の組織に所属しているかチェック
        if (!isOrganizationMember(userId, post.getOrganization().getId())) {
            return false;
        }

        // BoxTypeが設定されていない場合は組織メンバーなら閲覧可能
        if (post.getBoxType() == null) {
            return true;
        }

        // BoxPermissionで権限チェック
        return boxPermissionService.canViewBoxType(
                userId,
                post.getOrganization().getId(),
                post.getTenant().getId(),
                post.getBoxType().getId()
        );
    }

    /**
     * ユーザーが投稿に返信可能かチェック
     *
     * @param userId ユーザーID
     * @param post 投稿
     * @return 返信可能な場合true
     */
    public boolean canReplyToPost(Long userId, ProgressPost post) {
        if (post == null) {
            return false;
        }

        // まず投稿を閲覧できる必要がある
        if (!canViewPost(userId, post)) {
            return false;
        }

        // BoxTypeが設定されていない場合は組織メンバーなら返信可能（既にcanViewPostでチェック済み）
        if (post.getBoxType() == null) {
            return true;
        }

        // BoxPermissionで返信権限チェック
        return boxPermissionService.canPostToBoxType(
                userId,
                post.getOrganization().getId(),
                post.getTenant().getId(),
                post.getBoxType().getId()
        );
    }

    /**
     * ユーザーが組織のメンバーかチェック
     *
     * @param userId ユーザーID
     * @param organizationId 組織ID
     * @return メンバーの場合true
     */
    private boolean isOrganizationMember(Long userId, Long organizationId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }
        return organizationMemberRepository.findByUser(user).stream()
                .anyMatch(member -> member.getOrganization().getId().equals(organizationId));
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

    // ========================================
    // Box System関連メソッド
    // ========================================

    /**
     * BoxTypeで進捗投稿を取得
     *
     * @param boxType Box種別
     * @param pageable ページネーション情報
     * @return 投稿ページ
     */
    public Page<ProgressPost> getPostsByBoxType(BoxType boxType, Pageable pageable) {
        log.debug("Fetching posts for box type: {}", boxType.getBoxName());
        return progressPostRepository.findByBoxTypeOrderByCreatedAtDesc(boxType, pageable);
    }

    /**
     * テナントとBoxTypeで進捗投稿を取得
     *
     * @param tenant テナント
     * @param boxType Box種別
     * @param pageable ページネーション情報
     * @return 投稿ページ
     */
    public Page<ProgressPost> getPostsByTenantAndBoxType(Tenant tenant, BoxType boxType, Pageable pageable) {
        log.debug("Fetching posts for tenant {} and box type {}", tenant.getName(), boxType.getBoxName());
        return progressPostRepository.findByTenantAndBoxType(tenant, boxType, pageable);
    }

    /**
     * 複数のBoxTypeで進捗投稿を取得（階層的閲覧）
     *
     * @param tenant テナント
     * @param boxTypes Box種別リスト
     * @param pageable ページネーション情報
     * @return 投稿ページ
     */
    public Page<ProgressPost> getPostsByTenantAndBoxTypes(Tenant tenant, List<BoxType> boxTypes, Pageable pageable) {
        log.debug("Fetching posts for tenant {} with {} box types", tenant.getName(), boxTypes.size());
        return progressPostRepository.findByTenantAndBoxTypeIn(tenant, boxTypes, pageable);
    }

    /**
     * 組織とBoxTypeで進捗投稿を取得
     *
     * @param organization 組織
     * @param boxType Box種別
     * @param pageable ページネーション情報
     * @return 投稿ページ
     */
    public Page<ProgressPost> getPostsByOrganizationAndBoxType(
            Organization organization, BoxType boxType, Pageable pageable) {
        log.debug("Fetching posts for organization {} and box type {}",
                organization.getName(), boxType.getBoxName());
        return progressPostRepository.findByOrganizationAndBoxType(organization, boxType, pageable);
    }

    /**
     * テナント、BoxType、投稿タイプで進捗投稿を取得
     *
     * @param tenant テナント
     * @param boxType Box種別
     * @param postType 投稿タイプ
     * @param pageable ページネーション情報
     * @return 投稿ページ
     */
    public Page<ProgressPost> getPostsByTenantAndBoxTypeAndType(
            Tenant tenant, BoxType boxType, PostType postType, Pageable pageable) {
        log.debug("Fetching posts of type {} for tenant {} and box type {}",
                postType, tenant.getName(), boxType.getBoxName());
        return progressPostRepository.findByTenantAndBoxTypeAndPostType(tenant, boxType, postType, pageable);
    }

    /**
     * 日付範囲とBoxTypeで進捗投稿を取得
     *
     * @param tenant テナント
     * @param boxType Box種別
     * @param startDate 開始日
     * @param endDate 終了日
     * @return 投稿リスト
     */
    public List<ProgressPost> getPostsByBoxTypeAndDateRange(
            Tenant tenant, BoxType boxType, LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching posts for box type {} between {} and {}",
                boxType.getBoxName(), startDate, endDate);
        return progressPostRepository.findByTenantAndBoxTypeAndPostDateBetween(
                tenant, boxType, startDate, endDate);
    }

    /**
     * 返信投稿を取得
     *
     * @param parentPostId 親投稿ID
     * @return 返信投稿リスト
     */
    public List<ProgressPost> getRepliesByParentPostId(Long parentPostId) {
        log.debug("Fetching replies for parent post ID: {}", parentPostId);
        return progressPostRepository.findByParentPostId(parentPostId);
    }

    /**
     * 返信投稿を取得（ページネーション）
     *
     * @param parentPost 親投稿
     * @param pageable ページネーション情報
     * @return 返信投稿ページ
     */
    public Page<ProgressPost> getRepliesByParentPost(ProgressPost parentPost, Pageable pageable) {
        log.debug("Fetching replies for parent post ID: {}", parentPost.getId());
        return progressPostRepository.findByParentPostOrderByCreatedAtAsc(parentPost, pageable);
    }

    /**
     * ルート投稿のみを取得（返信ではない投稿）
     *
     * @param tenant テナント
     * @param pageable ページネーション情報
     * @return ルート投稿ページ
     */
    public Page<ProgressPost> getRootPostsByTenant(Tenant tenant, Pageable pageable) {
        log.debug("Fetching root posts for tenant: {}", tenant.getName());
        return progressPostRepository.findRootPostsByTenant(tenant, pageable);
    }

    /**
     * BoxTypeでルート投稿のみを取得
     *
     * @param tenant テナント
     * @param boxType Box種別
     * @param pageable ページネーション情報
     * @return ルート投稿ページ
     */
    public Page<ProgressPost> getRootPostsByTenantAndBoxType(
            Tenant tenant, BoxType boxType, Pageable pageable) {
        log.debug("Fetching root posts for tenant {} and box type {}",
                tenant.getName(), boxType.getBoxName());
        return progressPostRepository.findRootPostsByTenantAndBoxType(tenant, boxType, pageable);
    }

    /**
     * 返信投稿を作成
     *
     * @param parentPost 親投稿
     * @param replyPost 返信投稿
     * @return 作成された返信投稿
     */
    @Transactional
    public ProgressPost createReply(ProgressPost parentPost, ProgressPost replyPost) {
        log.info("Creating reply for parent post ID: {}", parentPost.getId());

        // 返信フラグと深さを設定
        replyPost.setParentPost(parentPost);
        replyPost.setIsReply(true);
        replyPost.setReplyDepth(parentPost.getReplyDepth() + 1);

        // 親投稿と同じBox、組織、テナントを継承
        replyPost.setBoxType(parentPost.getBoxType());
        replyPost.setOrganization(parentPost.getOrganization());
        replyPost.setTenant(parentPost.getTenant());

        // 返信投稿を保存
        ProgressPost savedReply = progressPostRepository.save(replyPost);

        // 親投稿の返信カウントをインクリメント
        parentPost.incrementReplyCount();
        progressPostRepository.save(parentPost);

        log.info("Reply created successfully with ID: {}", savedReply.getId());
        return savedReply;
    }

    /**
     * BoxTypeの投稿数をカウント
     *
     * @param boxType Box種別
     * @return 投稿数
     */
    public long countPostsByBoxType(BoxType boxType) {
        return progressPostRepository.countByBoxType(boxType);
    }

    /**
     * テナントとBoxTypeで投稿数をカウント
     *
     * @param tenant テナント
     * @param boxType Box種別
     * @return 投稿数
     */
    public long countPostsByTenantAndBoxType(Tenant tenant, BoxType boxType) {
        return progressPostRepository.countByTenantAndBoxType(tenant, boxType);
    }

    /**
     * テナントIDからデフォルトのBox種別を取得
     *
     * @param tenantId テナントID
     * @param boxName Box名（GENERAL_BOX, COMPANY_BOX等）
     * @return Box種別
     */
    public BoxType getDefaultBoxType(Long tenantId, String boxName) {
        return boxTypeRepository.findByTenantIdAndBoxName(tenantId, boxName)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Box type not found: " + boxName + " for tenant: " + tenantId));
    }

    /**
     * テナントのアクティブなBox種別を全て取得
     *
     * @param tenantId テナントID
     * @return Box種別リスト
     */
    public List<BoxType> getActiveBoxTypes(Long tenantId) {
        return boxTypeRepository.findByTenantIdAndIsActiveTrue(tenantId);
    }

    // ========================================
    // レスポンスDTO変換メソッド
    // ========================================

    /**
     * ProgressPostをProgressPostResponseに変換
     * 著者の所属組織情報を含める
     *
     * @param post 進捗投稿
     * @return レスポンスDTO
     */
    public ProgressPostResponse toResponse(ProgressPost post) {
        if (post == null) {
            return null;
        }

        // 著者の所属組織を取得
        List<OrganizationInfo> authorOrganizations = getAuthorOrganizations(post.getAuthor());

        return new ProgressPostResponse(post, authorOrganizations);
    }

    /**
     * ProgressPostのページをProgressPostResponseのページに変換
     *
     * @param postsPage 進捗投稿ページ
     * @return レスポンスDTOページ
     */
    public Page<ProgressPostResponse> toResponsePage(Page<ProgressPost> postsPage) {
        List<ProgressPostResponse> responsePosts = postsPage.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(responsePosts, postsPage.getPageable(), postsPage.getTotalElements());
    }

    /**
     * ProgressPostのリストをProgressPostResponseのリストに変換
     *
     * @param posts 進捗投稿リスト
     * @return レスポンスDTOリスト
     */
    public List<ProgressPostResponse> toResponseList(List<ProgressPost> posts) {
        return posts.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 著者の所属組織情報を取得
     *
     * @param author 著者
     * @return 所属組織情報のリスト
     */
    private List<OrganizationInfo> getAuthorOrganizations(User author) {
        if (author == null) {
            return List.of();
        }

        // ユーザーが所属している組織を取得
        List<OrganizationMember> memberships = organizationMemberRepository.findByUser(author);

        return memberships.stream()
                .map(membership -> {
                    Organization org = membership.getOrganization();
                    return new OrganizationInfo(
                            org.getId(),
                            org.getName(),
                            org.getOrganizationType(),
                            org.getLevel(),
                            membership.getRole() != null ? membership.getRole().name() : null
                    );
                })
                .collect(Collectors.toList());
    }
}
