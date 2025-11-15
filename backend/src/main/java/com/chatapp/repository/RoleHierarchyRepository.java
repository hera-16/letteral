package com.chatapp.repository;

import com.chatapp.model.RoleHierarchy;
import com.chatapp.model.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 権限階級リポジトリ
 */
@Repository
public interface RoleHierarchyRepository extends JpaRepository<RoleHierarchy, Long> {

    /**
     * テナント別に全権限階級を取得（レベル降順）
     */
    List<RoleHierarchy> findByTenantIdOrderByRoleLevelDesc(Long tenantId);

    /**
     * テナントと役職名で権限階級を取得
     */
    Optional<RoleHierarchy> findByTenantIdAndRoleName(Long tenantId, RoleName roleName);

    /**
     * テナントと権限レベルで権限階級を取得
     */
    Optional<RoleHierarchy> findByTenantIdAndRoleLevel(Long tenantId, Integer roleLevel);

    /**
     * 指定レベル以上の権限階級を取得
     */
    @Query("SELECT rh FROM RoleHierarchy rh WHERE rh.tenantId = :tenantId AND rh.roleLevel >= :minLevel ORDER BY rh.roleLevel ASC")
    List<RoleHierarchy> findByTenantIdAndRoleLevelGreaterThanEqual(@Param("tenantId") Long tenantId, @Param("minLevel") Integer minLevel);

    /**
     * 昇格可能な次の権限レベルを取得
     */
    @Query("SELECT rh FROM RoleHierarchy rh WHERE rh.tenantId = :tenantId AND rh.roleLevel > :currentLevel ORDER BY rh.roleLevel ASC")
    Optional<RoleHierarchy> findNextPromotionLevel(@Param("tenantId") Long tenantId, @Param("currentLevel") Integer currentLevel);
}
