package com.chatapp.repository;

import com.chatapp.model.ChallengeShare;
import com.chatapp.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ChallengeShareRepository extends JpaRepository<ChallengeShare, Long> {

    @EntityGraph(attributePaths = {"user", "challenge"})
    Page<ChallengeShare> findAllByOrderBySharedAtDesc(Pageable pageable);

    long countByUserNot(User user);

    long countByUserNotAndSharedAtAfter(User user, LocalDateTime sharedAt);
}
