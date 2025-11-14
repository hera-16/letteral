package com.chatapp.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * メソッドに必要な権限を指定するアノテーション
 * コントローラーのメソッドに付与して、アクセス制御を行います
 *
 * 使用例:
 * {@code @RequirePermission(Permission.USER_CREATE)}
 * public User createUser(@RequestBody UserDTO dto) { ... }
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {

    /**
     * 必要な権限
     * 複数指定した場合は、いずれか1つを持っていればアクセス可能
     */
    Permission[] value();

    /**
     * すべての権限が必要かどうか
     * true の場合、指定されたすべての権限を持っている必要がある
     * false の場合（デフォルト）、いずれか1つの権限があればアクセス可能
     */
    boolean requireAll() default false;

    /**
     * 権限チェックに失敗した場合のエラーメッセージ
     */
    String message() default "この操作を実行する権限がありません";
}
