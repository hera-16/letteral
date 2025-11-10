package com.chatapp.repository;

import com.chatapp.model.OkrKeyResult;
import com.chatapp.model.OkrObjective;
import com.chatapp.model.Tenant;
import com.chatapp.model.enums.OkrStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * OKR主要結果リポジトリ
 */
@Repository
public interface OkrKeyResultRepository extends JpaRepository<OkrKeyResult, Long> {

    List<OkrKeyResult> findByObjectiveOrderByCreatedAtAsc(OkrObjective objective);

    List<OkrKeyResult> findByObjectiveAndStatusOrderByCreatedAtAsc(OkrObjective objective, OkrStatus status);

    Optional<OkrKeyResult> findByIdAndTenant(Long id, Tenant tenant);

    @Query("SELECT kr FROM OkrKeyResult kr WHERE kr.objective = :objective AND kr.status = 'ACTIVE' ORDER BY kr.progressRate ASC")
    List<OkrKeyResult> findActiveKeyResultsByObjectiveOrderByProgress(@Param("objective") OkrObjective objective);

    @Query("SELECT AVG(kr.progressRate) FROM OkrKeyResult kr WHERE kr.objective = :objective AND kr.status = 'ACTIVE'")
    Double calculateAverageProgressForObjective(@Param("objective") OkrObjective objective);

    Long countByObjective(OkrObjective objective);

    Long countByObjectiveAndStatus(OkrObjective objective, OkrStatus status);
}
