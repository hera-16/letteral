package com.chatapp.interceptor;

import com.chatapp.annotation.RequireRole;
import com.chatapp.model.RoleName;
import com.chatapp.model.User;
import com.chatapp.repository.UserRepository;
import com.chatapp.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * ロールベースの権限チェックを行うインターセプター
 */
@Component
public class RoleAuthorizationInterceptor implements HandlerInterceptor {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // ハンドラがメソッドでない場合はスキップ
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);

        // アノテーションがない場合はスキップ
        if (requireRole == null) {
            return true;
        }

        // 認証情報を取得
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "認証が必要です");
            return false;
        }

        String username = authentication.getName();
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (!userOpt.isPresent()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "ユーザーが見つかりません");
            return false;
        }

        User user = userOpt.get();

        // ユーザーのロールを取得
        var userRoles = userRoleRepository.findByUserId(user.getId());

        if (userRoles.isEmpty()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "ロールが設定されていません");
            return false;
        }

        RoleName requiredRole = requireRole.value();
        RoleName userRole = userRoles.get(0).getRoleName();

        // ロール階層をチェック
        if (!hasRequiredRole(userRole, requiredRole)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                String.format("この操作には%sロールが必要です", requiredRole));
            return false;
        }

        return true;
    }

    /**
     * ユーザーのロールが必要なロールを満たしているかチェック
     */
    private boolean hasRequiredRole(RoleName userRole, RoleName requiredRole) {
        return userRole.isAtLeast(requiredRole);
    }
}
