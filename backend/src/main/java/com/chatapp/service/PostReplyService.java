package com.chatapp.service;

import com.chatapp.dto.CreatePostReplyRequest;
import com.chatapp.dto.PostReplyDTO;
import com.chatapp.model.PostReply;
import com.chatapp.model.ProgressPost;
import com.chatapp.model.User;
import com.chatapp.model.enums.OrganizationRole;
import com.chatapp.repository.PostReplyRepository;
import com.chatapp.repository.ProgressPostRepository;
import com.chatapp.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 投稿返信サービス
 * 返信の作成、取得、権限チェックなどのビジネスロジックを提供
 */
@Service
@Transactional(readOnly = true)
public class PostReplyService {

    private static final Logger log = LoggerFactory.getLogger(PostReplyService.class);

    private final PostReplyRepository postReplyRepository;
    private final ProgressPostRepository progressPostRepository;
    private final UserRepository userRepository;
    private final OrganizationPermissionService organizationPermissionService;

    public PostReplyService(
            PostReplyRepository postReplyRepository,
            ProgressPostRepository progressPostRepository,
            UserRepository userRepository,
            OrganizationPermissionService organizationPermissionService) {
        this.postReplyRepository = postReplyRepository;
        this.progressPostRepository = progressPostRepository;
        this.userRepository = userRepository;
        this.organizationPermissionService = organizationPermissionService;
    }

    /**
     * 返信を作成
     * 権限チェック: 投稿本人または管理者権限が必要
     */
    @Transactional
    public PostReplyDTO createReply(Long userId, CreatePostReplyRequest request) {
        log.info("Creating reply for post {} by user {}", request.getPostId(), userId);

        // 投稿とユーザーを取得（authorとorganizationも一緒に取得）
        ProgressPost post = progressPostRepository.findById(request.getPostId())
                .orElseThrow(() -> new EntityNotFoundException("Post not found with ID: " + request.getPostId()));

        // Lazy loadingのために明示的にauthorとorganizationを読み込む
        post.getAuthor().getId(); // Force initialization
        post.getOrganization().getId(); // Force initialization

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        // 返信権限チェック
        if (!canReplyToPost(user, post)) {
            log.warn("User {} does not have permission to reply to post {}", userId, request.getPostId());
            throw new AccessDeniedException("この投稿への返信権限がありません。投稿者本人または組織管理者のみが返信できます。");
        }

        // 返信を作成
        PostReply reply = new PostReply();
        reply.setPost(post);
        reply.setAuthor(user);
        reply.setContent(request.getContent());

        PostReply savedReply = postReplyRepository.save(reply);
        log.info("Reply created with ID: {}", savedReply.getId());

        return convertToDTO(savedReply);
    }

    /**
     * 特定の投稿に対する返信を取得
     * 閲覧権限チェック: 投稿本人または管理者権限が必要
     */
    public List<PostReplyDTO> getRepliesByPostId(Long postId, Long viewerId) {
        log.info("Getting replies for post {} by viewer {}", postId, viewerId);

        ProgressPost post = progressPostRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found with ID: " + postId));

        User viewer = userRepository.findById(viewerId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + viewerId));

        // 閲覧権限チェック
        if (!canViewReplies(viewer, post)) {
            log.warn("User {} does not have permission to view replies for post {}", viewerId, postId);
            return List.of(); // 権限がない場合は空リストを返す
        }

        List<PostReply> replies = postReplyRepository.findByPostIdOrderByCreatedAtAsc(postId);
        return replies.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 返信数を取得
     */
    public long getReplyCount(Long postId) {
        return postReplyRepository.countByPostId(postId);
    }

    /**
     * 返信権限チェック
     * - 投稿の本人
     * - 組織の管理者権限を持つユーザー
     */
    private boolean canReplyToPost(User user, ProgressPost post) {
        // 投稿本人
        if (post.getAuthor().getId().equals(user.getId())) {
            return true;
        }

        // 組織の管理者権限チェック
        return hasManagerRoleInOrganization(user, post.getOrganization().getId());
    }

    /**
     * 返信閲覧権限チェック
     * - 投稿の本人
     * - 組織の管理者権限を持つユーザー
     */
    private boolean canViewReplies(User user, ProgressPost post) {
        // 投稿本人
        if (post.getAuthor().getId().equals(user.getId())) {
            return true;
        }

        // 組織の管理者権限チェック
        return hasManagerRoleInOrganization(user, post.getOrganization().getId());
    }

    /**
     * 組織内での管理者権限チェック
     */
    private boolean hasManagerRoleInOrganization(User user, Long organizationId) {
        return organizationPermissionService.getUserRole(user, organizationId)
                .map(role -> role == OrganizationRole.ADMIN_SUPER
                        || role == OrganizationRole.ADMIN_LEAD
                        || role == OrganizationRole.ADMIN_CORE
                        || role == OrganizationRole.ADMIN_ROOT
                        || role == OrganizationRole.OWNER)
                .orElse(false);
    }

    /**
     * PostReplyエンティティをDTOに変換
     */
    private PostReplyDTO convertToDTO(PostReply reply) {
        return new PostReplyDTO(
                reply.getId(),
                reply.getPost().getId(),
                reply.getAuthor().getId(),
                reply.getAuthor().getUsername(),
                reply.getContent(),
                reply.getCreatedAt(),
                reply.getUpdatedAt()
        );
    }
}
