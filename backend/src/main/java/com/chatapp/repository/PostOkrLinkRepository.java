package com.chatapp.repository;

import com.chatapp.model.OkrKeyResult;
import com.chatapp.model.OkrObjective;
import com.chatapp.model.PostOkrLink;
import com.chatapp.model.ProgressPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 投稿とOKRの紐付けリポジトリ
 */
@Repository
public interface PostOkrLinkRepository extends JpaRepository<PostOkrLink, Long> {

    List<PostOkrLink> findByPost(ProgressPost post);

    List<PostOkrLink> findByObjective(OkrObjective objective);

    List<PostOkrLink> findByKeyResult(OkrKeyResult keyResult);

    @Query("SELECT pol FROM PostOkrLink pol WHERE pol.objective = :objective OR pol.keyResult IN (SELECT kr FROM OkrKeyResult kr WHERE kr.objective = :objective) ORDER BY pol.createdAt DESC")
    List<PostOkrLink> findByObjectiveOrKeyResultObjective(@Param("objective") OkrObjective objective);

    Long countByObjective(OkrObjective objective);

    Long countByKeyResult(OkrKeyResult keyResult);

    @Query("SELECT COUNT(pol) FROM PostOkrLink pol WHERE pol.objective = :objective OR pol.keyResult IN (SELECT kr FROM OkrKeyResult kr WHERE kr.objective = :objective)")
    Long countByObjectiveIncludingKeyResults(@Param("objective") OkrObjective objective);
}
