package com.chatapp.repository;

import com.chatapp.model.PostReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostReplyRepository extends JpaRepository<PostReply, Long> {

    /**
     * 特定の投稿に対する全ての返信を取得
     */
    List<PostReply> findByPostIdOrderByCreatedAtAsc(Long postId);

    /**
     * 特定の投稿に対する返信数を取得
     */
    long countByPostId(Long postId);

    /**
     * 特定のユーザーが作成した返信を取得
     */
    List<PostReply> findByAuthorIdOrderByCreatedAtDesc(Long authorId);
}
