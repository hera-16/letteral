package com.chatapp.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * メソッドに必要なロールを指定するアノテーション
 * コントローラーのメソッドに付与して、ロールベースのアクセス制御を行います
 *
 * 使用例:
 * {@code @RequireRole(UserRole.TENANT_ADMIN)}
 * public List<User> getAllUsers() { ... }
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {

    /**
     * 必要なロール
     * 複数指定した場合は、いずれか1つを持っていればアクセス可能
     */
    UserRole[] value();

    /**
     * 権限チェックに失敗した場合のエラーメッセージ
     */
    String message() default "この操作を実行するロールがありません";
}
