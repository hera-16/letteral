package com.chatapp.repository;

import com.chatapp.model.PostReaction;
import com.chatapp.model.ProgressPost;
import com.chatapp.model.User;
import com.chatapp.model.enums.ReactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 投稿リアクションリポジトリ
 */
@Repository
public interface PostReactionRepository extends JpaRepository<PostReaction, Long> {

    List<PostReaction> findByPost(ProgressPost post);

    List<PostReaction> findByPostAndReactionType(ProgressPost post, ReactionType reactionType);

    Optional<PostReaction> findByPostAndUserAndReactionType(ProgressPost post, User user, ReactionType reactionType);

    Boolean existsByPostAndUserAndReactionType(ProgressPost post, User user, ReactionType reactionType);

    Long countByPost(ProgressPost post);

    Long countByPostAndReactionType(ProgressPost post, ReactionType reactionType);

    @Query("SELECT pr FROM PostReaction pr WHERE pr.post IN :posts ORDER BY pr.createdAt DESC")
    List<PostReaction> findByPostIn(@Param("posts") List<ProgressPost> posts);

    @Query("SELECT pr.reactionType, COUNT(pr) FROM PostReaction pr WHERE pr.post = :post GROUP BY pr.reactionType")
    List<Object[]> countByPostGroupByReactionType(@Param("post") ProgressPost post);
}
