package com.chatapp.service;

import com.chatapp.model.PostReaction;
import com.chatapp.model.ProgressPost;
import com.chatapp.model.User;
import com.chatapp.model.enums.ReactionType;
import com.chatapp.repository.PostReactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 投稿リアクションサービス
 * リアクションの追加、削除、集計などのビジネスロジックを提供
 */
@Service
@Transactional(readOnly = true)
public class PostReactionService {

    private static final Logger log = LoggerFactory.getLogger(PostReactionService.class);
    private final PostReactionRepository postReactionRepository;
    private final ProgressPostService progressPostService;

    public PostReactionService(PostReactionRepository postReactionRepository,
                               ProgressPostService progressPostService) {
        this.postReactionRepository = postReactionRepository;
        this.progressPostService = progressPostService;
    }

    /**
     * リアクション追加
     *
     * @param reaction 追加するリアクション
     * @return 追加されたリアクション
     * @throws IllegalStateException 同じリアクションが既に存在する場合
     */
    @Transactional
    public PostReaction addReaction(PostReaction reaction) {
        log.info("Adding reaction {} to post {} by user {}",
                 reaction.getReactionType(), reaction.getPost().getId(), reaction.getUser().getId());

        // 重複チェック
        boolean exists = postReactionRepository.existsByPostAndUserAndReactionType(
                reaction.getPost(), reaction.getUser(), reaction.getReactionType());

        if (exists) {
            throw new IllegalStateException("User already added this reaction to the post");
        }

        reaction.setCreatedAt(LocalDateTime.now());
        PostReaction savedReaction = postReactionRepository.save(reaction);

        // 投稿のリアクションカウントを増やす
        progressPostService.incrementReactionCount(reaction.getPost().getId());

        log.info("Reaction added successfully with ID: {}", savedReaction.getId());
        return savedReaction;
    }

    /**
     * リアクションID取得
     *
     * @param id リアクションID
     * @return リアクション情報
     * @throws EntityNotFoundException リアクションが見つからない場合
     */
    public PostReaction getReactionById(Long id) {
        log.debug("Fetching reaction by ID: {}", id);
        return postReactionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reaction not found with ID: " + id));
    }

    /**
     * 投稿の全リアクション取得
     *
     * @param post 投稿
     * @return リアクションリスト
     */
    public List<PostReaction> getReactionsByPost(ProgressPost post) {
        log.debug("Fetching all reactions for post: {}", post.getId());
        return postReactionRepository.findByPost(post);
    }

    /**
     * 投稿の特定タイプのリアクション取得
     *
     * @param post 投稿
     * @param reactionType リアクションタイプ
     * @return リアクションリスト
     */
    public List<PostReaction> getReactionsByPostAndType(ProgressPost post, ReactionType reactionType) {
        log.debug("Fetching reactions of type {} for post {}", reactionType, post.getId());
        return postReactionRepository.findByPostAndReactionType(post, reactionType);
    }

    /**
     * ユーザーの特定投稿へのリアクション取得
     *
     * @param post 投稿
     * @param user ユーザー
     * @param reactionType リアクションタイプ
     * @return リアクション
     */
    public Optional<PostReaction> getUserReaction(ProgressPost post, User user, ReactionType reactionType) {
        log.debug("Fetching reaction from user {} on post {} with type {}",
                  user.getId(), post.getId(), reactionType);
        return postReactionRepository.findByPostAndUserAndReactionType(post, user, reactionType);
    }

    /**
     * 複数投稿のリアクション一括取得
     *
     * @param posts 投稿リスト
     * @return リアクションリスト
     */
    public List<PostReaction> getReactionsByPosts(List<ProgressPost> posts) {
        log.debug("Fetching reactions for {} posts", posts.size());
        return postReactionRepository.findByPostIn(posts);
    }

    /**
     * 投稿のリアクション数取得
     *
     * @param post 投稿
     * @return リアクション数
     */
    public long countReactionsByPost(ProgressPost post) {
        return postReactionRepository.countByPost(post);
    }

    /**
     * 投稿の特定タイプのリアクション数取得
     *
     * @param post 投稿
     * @param reactionType リアクションタイプ
     * @return リアクション数
     */
    public long countReactionsByPostAndType(ProgressPost post, ReactionType reactionType) {
        return postReactionRepository.countByPostAndReactionType(post, reactionType);
    }

    /**
     * 投稿のリアクション集計（タイプ別）
     *
     * @param post 投稿
     * @return リアクションタイプごとのカウントマップ
     */
    public Map<ReactionType, Long> getReactionSummary(ProgressPost post) {
        log.debug("Getting reaction summary for post: {}", post.getId());

        List<Object[]> results = postReactionRepository.countByPostGroupByReactionType(post);
        Map<ReactionType, Long> summary = new HashMap<>();

        for (Object[] result : results) {
            ReactionType type = (ReactionType) result[0];
            Long count = (Long) result[1];
            summary.put(type, count);
        }

        log.debug("Reaction summary: {}", summary);
        return summary;
    }

    /**
     * リアクション削除
     *
     * @param id リアクションID
     */
    @Transactional
    public void deleteReaction(Long id) {
        log.info("Deleting reaction ID: {}", id);

        PostReaction reaction = getReactionById(id);
        Long postId = reaction.getPost().getId();

        postReactionRepository.deleteById(id);

        // 投稿のリアクションカウントを減らす
        progressPostService.decrementReactionCount(postId);

        log.info("Reaction deleted successfully: {}", id);
    }

    /**
     * ユーザーのリアクション削除
     *
     * @param post 投稿
     * @param user ユーザー
     * @param reactionType リアクションタイプ
     * @throws EntityNotFoundException リアクションが見つからない場合
     */
    @Transactional
    public void deleteUserReaction(ProgressPost post, User user, ReactionType reactionType) {
        log.info("Deleting reaction from user {} on post {} with type {}",
                 user.getId(), post.getId(), reactionType);

        Optional<PostReaction> reaction = getUserReaction(post, user, reactionType);
        if (reaction.isEmpty()) {
            throw new EntityNotFoundException("Reaction not found");
        }

        deleteReaction(reaction.get().getId());
    }

    /**
     * ユーザーが既にリアクションしているか確認
     *
     * @param post 投稿
     * @param user ユーザー
     * @param reactionType リアクションタイプ
     * @return リアクション済みの場合true
     */
    public boolean hasUserReacted(ProgressPost post, User user, ReactionType reactionType) {
        return postReactionRepository.existsByPostAndUserAndReactionType(post, user, reactionType);
    }

    /**
     * リアクションのトグル（追加/削除）
     * 既にリアクションしている場合は削除、していない場合は追加
     *
     * @param post 投稿
     * @param user ユーザー
     * @param reactionType リアクションタイプ
     * @return 追加された場合はリアクション、削除された場合はnull
     */
    @Transactional
    public PostReaction toggleReaction(ProgressPost post, User user, ReactionType reactionType) {
        log.info("Toggling reaction {} from user {} on post {}",
                 reactionType, user.getId(), post.getId());

        Optional<PostReaction> existing = getUserReaction(post, user, reactionType);

        if (existing.isPresent()) {
            // 既存のリアクションを削除
            deleteReaction(existing.get().getId());
            log.info("Reaction removed");
            return null;
        } else {
            // 新しいリアクションを追加
            PostReaction newReaction = new PostReaction(post.getTenant(), post, user, reactionType);
            PostReaction saved = addReaction(newReaction);
            log.info("Reaction added");
            return saved;
        }
    }
}
