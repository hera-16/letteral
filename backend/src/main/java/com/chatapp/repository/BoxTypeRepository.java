package com.chatapp.repository;

import com.chatapp.model.BoxType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Box種別リポジトリ
 */
@Repository
public interface BoxTypeRepository extends JpaRepository<BoxType, Long> {

    /**
     * テナント別にアクティブなBox種別を取得
     */
    List<BoxType> findByTenantIdAndIsActiveTrue(Long tenantId);

    /**
     * テナント別に全Box種別を取得
     */
    List<BoxType> findByTenantId(Long tenantId);

    /**
     * テナントとBox名でBox種別を取得
     */
    Optional<BoxType> findByTenantIdAndBoxName(Long tenantId, String boxName);

    /**
     * システム標準Boxを取得
     */
    List<BoxType> findByTenantIdAndIsSystemBoxTrue(Long tenantId);

    /**
     * カスタムBoxを取得
     */
    List<BoxType> findByTenantIdAndIsSystemBoxFalse(Long tenantId);

    /**
     * Box名の存在チェック
     */
    boolean existsByTenantIdAndBoxName(Long tenantId, String boxName);
}
