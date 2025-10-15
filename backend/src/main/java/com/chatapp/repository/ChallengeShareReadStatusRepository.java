package com.chatapp.repository;

import com.chatapp.model.ChallengeShareReadStatus;
import com.chatapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChallengeShareReadStatusRepository extends JpaRepository<ChallengeShareReadStatus, Long> {

    Optional<ChallengeShareReadStatus> findByUser(User user);
}
