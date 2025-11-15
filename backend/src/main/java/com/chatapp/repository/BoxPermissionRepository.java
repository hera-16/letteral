package com.chatapp.repository;

import com.chatapp.model.BoxPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Box閲覧権限リポジトリ
 */
@Repository
public interface BoxPermissionRepository extends JpaRepository<BoxPermission, Long> {

    /**
     * ユーザー、Box種別、組織で権限を取得
     */
    Optional<BoxPermission> findByUserIdAndBoxTypeIdAndOrganizationId(Long userId, Long boxTypeId, Long organizationId);

    /**
     * ユーザーと組織で全権限を取得
     */
    List<BoxPermission> findByUserIdAndOrganizationId(Long userId, Long organizationId);

    /**
     * ユーザー別に全権限を取得
     */
    List<BoxPermission> findByUserId(Long userId);

    /**
     * Box種別別に全権限を取得
     */
    List<BoxPermission> findByBoxTypeId(Long boxTypeId);

    /**
     * ユーザーが閲覧可能なBox一覧を取得
     */
    @Query("SELECT bp FROM BoxPermission bp WHERE bp.userId = :userId AND bp.organizationId = :organizationId AND bp.canView = true")
    List<BoxPermission> findViewableBoxesByUser(@Param("userId") Long userId, @Param("organizationId") Long organizationId);

    /**
     * ユーザーが投稿可能なBox一覧を取得
     */
    @Query("SELECT bp FROM BoxPermission bp WHERE bp.userId = :userId AND bp.organizationId = :organizationId AND bp.canPost = true")
    List<BoxPermission> findPostableBoxesByUser(@Param("userId") Long userId, @Param("organizationId") Long organizationId);

    /**
     * ユーザーがBox閲覧可能かチェック
     */
    @Query("SELECT CASE WHEN COUNT(bp) > 0 THEN true ELSE false END FROM BoxPermission bp WHERE bp.userId = :userId AND bp.boxTypeId = :boxTypeId AND bp.organizationId = :organizationId AND bp.canView = true")
    boolean canUserViewBox(@Param("userId") Long userId, @Param("boxTypeId") Long boxTypeId, @Param("organizationId") Long organizationId);

    /**
     * ユーザーがBox投稿可能かチェック
     */
    @Query("SELECT CASE WHEN COUNT(bp) > 0 THEN true ELSE false END FROM BoxPermission bp WHERE bp.userId = :userId AND bp.boxTypeId = :boxTypeId AND bp.organizationId = :organizationId AND bp.canPost = true")
    boolean canUserPostToBox(@Param("userId") Long userId, @Param("boxTypeId") Long boxTypeId, @Param("organizationId") Long organizationId);

    /**
     * 有効期限切れの権限を削除
     */
    @Query("DELETE FROM BoxPermission bp WHERE bp.expiresAt IS NOT NULL AND bp.expiresAt < CURRENT_TIMESTAMP")
    void deleteExpiredPermissions();

    /**
     * ユーザーとボックスタイプIDで権限を取得
     */
    Optional<BoxPermission> findByUserIdAndBoxTypeId(Long userId, Long boxTypeId);
}
