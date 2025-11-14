package com.chatapp.security;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 権限チェックアノテーションを処理するAspect
 * @RequirePermission と @RequireRole アノテーションが付与されたメソッドの実行前に権限をチェックします
 */
@Aspect
@Component
public class PermissionAspect {

    @Autowired
    private PermissionChecker permissionChecker;

    /**
     * @RequirePermission アノテーションの処理
     * メソッド実行前に指定された権限を持っているかチェック
     */
    @Before("@annotation(com.chatapp.security.RequirePermission)")
    public void checkPermission(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        RequirePermission annotation = method.getAnnotation(RequirePermission.class);
        if (annotation == null) {
            return;
        }

        Permission[] requiredPermissions = annotation.value();
        boolean requireAll = annotation.requireAll();
        String message = annotation.message();

        boolean hasAccess;
        if (requireAll) {
            // すべての権限が必要
            hasAccess = permissionChecker.hasAllPermissions(requiredPermissions);
        } else {
            // いずれかの権限があればOK
            hasAccess = permissionChecker.hasAnyPermission(requiredPermissions);
        }

        if (!hasAccess) {
            throw new AccessDeniedException(message);
        }
    }

    /**
     * @RequireRole アノテーションの処理
     * メソッド実行前に指定されたロールを持っているかチェック
     */
    @Before("@annotation(com.chatapp.security.RequireRole)")
    public void checkRole(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        RequireRole annotation = method.getAnnotation(RequireRole.class);
        if (annotation == null) {
            return;
        }

        UserRole[] requiredRoles = annotation.value();
        String message = annotation.message();

        boolean hasRole = permissionChecker.hasAnyRole(requiredRoles);

        if (!hasRole) {
            throw new AccessDeniedException(message);
        }
    }
}
