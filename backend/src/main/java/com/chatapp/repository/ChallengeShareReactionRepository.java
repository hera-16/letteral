package com.chatapp.repository;

import com.chatapp.model.ChallengeShareReaction;
import com.chatapp.model.ChallengeShareReaction.ReactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChallengeShareReactionRepository extends JpaRepository<ChallengeShareReaction, Long> {

    Optional<ChallengeShareReaction> findByShareIdAndUserId(Long shareId, Long userId);

    List<ChallengeShareReaction> findByShareIdInAndUserId(List<Long> shareIds, Long userId);

    void deleteByShareIdAndUserId(Long shareId, Long userId);

    @Query("SELECT r.share.id AS shareId, r.type AS type, COUNT(r) AS count " +
            "FROM ChallengeShareReaction r WHERE r.share.id IN :shareIds " +
            "GROUP BY r.share.id, r.type")
    List<ReactionCountProjection> countByShareIds(@Param("shareIds") List<Long> shareIds);

    interface ReactionCountProjection {
        Long getShareId();
        ReactionType getType();
        Long getCount();
    }
}
