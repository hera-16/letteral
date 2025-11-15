package com.chatapp.repository;

import com.chatapp.model.RolePromotionRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 権限昇格申請リポジトリ
 */
@Repository
public interface RolePromotionRequestRepository extends JpaRepository<RolePromotionRequest, Long> {

    /**
     * ユーザーの全申請を取得（新しい順）
     */
    List<RolePromotionRequest> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 組織の全申請を取得（新しい順）
     */
    List<RolePromotionRequest> findByOrganizationIdOrderByCreatedAtDesc(Long organizationId);

    /**
     * ステータス別に申請を取得
     */
    List<RolePromotionRequest> findByTenantIdAndStatusOrderByCreatedAtDesc(Long tenantId, String status);

    /**
     * ユーザーと組織の保留中申請を取得
     */
    Optional<RolePromotionRequest> findByUserIdAndOrganizationIdAndStatus(Long userId, Long organizationId, String status);

    /**
     * 保留中の申請一覧をページングで取得
     */
    @Query("SELECT rpr FROM RolePromotionRequest rpr WHERE rpr.tenantId = :tenantId AND rpr.status = 'PENDING' ORDER BY rpr.createdAt ASC")
    Page<RolePromotionRequest> findPendingRequests(@Param("tenantId") Long tenantId, Pageable pageable);

    /**
     * 組織の保留中申請数を取得
     */
    @Query("SELECT COUNT(rpr) FROM RolePromotionRequest rpr WHERE rpr.organizationId = :organizationId AND rpr.status = 'PENDING'")
    Long countPendingRequestsByOrganization(@Param("organizationId") Long organizationId);

    /**
     * ユーザーの保留中申請があるかチェック
     */
    @Query("SELECT CASE WHEN COUNT(rpr) > 0 THEN true ELSE false END FROM RolePromotionRequest rpr WHERE rpr.userId = :userId AND rpr.organizationId = :organizationId AND rpr.status = 'PENDING'")
    boolean hasPendingRequest(@Param("userId") Long userId, @Param("organizationId") Long organizationId);
}
