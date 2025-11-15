package com.chatapp.annotation;

import java.lang.annotation.*;

/**
 * コントローラーメソッドに必要なボックスアクセス権限を指定するアノテーション
 * このアノテーションが付いたメソッドは、指定されたボックスへのアクセス権限を持つユーザーのみがアクセスできます
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireBoxAccess {
    /**
     * 必要な権限レベル（view, post, reply, moderate）
     * @return 権限レベル
     */
    String permission() default "view";

    /**
     * ボックスタイプIDのパラメータ名
     * @return リクエストパラメータ名
     */
    String boxTypeParam() default "boxTypeId";

    /**
     * 組織IDのパラメータ名
     * @return リクエストパラメータ名
     */
    String orgParam() default "organizationId";
}
