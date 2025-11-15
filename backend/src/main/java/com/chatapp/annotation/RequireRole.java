package com.chatapp.annotation;

import com.chatapp.model.RoleName;
import java.lang.annotation.*;

/**
 * コントローラーメソッドに必要な最小ロールを指定するアノテーション
 * このアノテーションが付いたメソッドは、指定されたロール以上の権限を持つユーザーのみがアクセスできます
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {
    /**
     * 必要な最小ロール
     * @return ロール名
     */
    RoleName value();

    /**
     * ロールチェックをスキップする条件（オプション）
     * @return スキップする場合はtrue
     */
    boolean skipIfOwner() default false;
}
