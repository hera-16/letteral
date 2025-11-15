package com.chatapp.repository;

import com.chatapp.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ユーザー権限リポジトリ
 */
@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    /**
     * ユーザーと組織でユーザー権限を取得
     */
    Optional<UserRole> findByUserIdAndOrganizationId(Long userId, Long organizationId);

    /**
     * ユーザー別に全権限を取得
     */
    List<UserRole> findByUserId(Long userId);

    /**
     * 組織別に全ユーザー権限を取得（権限レベル降順）
     */
    List<UserRole> findByOrganizationIdOrderByRoleLevelDesc(Long organizationId);

    /**
     * テナント別に全ユーザー権限を取得
     */
    List<UserRole> findByTenantId(Long tenantId);

    /**
     * 承認待ち状態のユーザー権限を取得
     */
    List<UserRole> findByTenantIdAndApprovalStatus(Long tenantId, String approvalStatus);

    /**
     * 指定レベル以上のユーザーを取得
     */
    @Query("SELECT ur FROM UserRole ur WHERE ur.organizationId = :organizationId AND ur.roleLevel >= :minLevel ORDER BY ur.roleLevel DESC")
    List<UserRole> findByOrganizationIdAndRoleLevelGreaterThanEqual(@Param("organizationId") Long organizationId, @Param("minLevel") Integer minLevel);

    /**
     * ユーザーの投稿数をカウント
     */
    @Query("SELECT COUNT(p) FROM ProgressPost p WHERE p.author.id = :userId")
    Long countPostsByUserId(@Param("userId") Long userId);
}
