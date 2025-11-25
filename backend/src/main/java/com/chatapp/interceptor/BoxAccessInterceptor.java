package com.chatapp.interceptor;

import com.chatapp.annotation.RequireBoxAccess;
import com.chatapp.model.BoxPermission;
import com.chatapp.model.User;
import com.chatapp.repository.BoxPermissionRepository;
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
 * ボックスアクセス権限をチェックするインターセプター
 */
@Component
public class BoxAccessInterceptor implements HandlerInterceptor {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private BoxPermissionRepository boxPermissionRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // ハンドラがメソッドでない場合はスキップ
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequireBoxAccess requireBoxAccess = handlerMethod.getMethodAnnotation(RequireBoxAccess.class);

        // アノテーションがない場合はスキップ
        if (requireBoxAccess == null) {
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

        // Get the first user role (future: handle multiple roles)
        // UserRole userRole = userRoles.get(0);
        String requiredPermission = requireBoxAccess.permission();

        // ボックスタイプIDを取得
        String boxTypeParam = requireBoxAccess.boxTypeParam();
        String boxTypeIdStr = request.getParameter(boxTypeParam);

        if (boxTypeIdStr == null) {
            // パスパラメータからも取得を試みる
            boxTypeIdStr = request.getAttribute(boxTypeParam) != null ?
                request.getAttribute(boxTypeParam).toString() : null;
        }

        // 組織IDを取得
        String orgParam = requireBoxAccess.orgParam();
        String orgIdStr = request.getParameter(orgParam);

        if (orgIdStr == null) {
            // パスパラメータからも取得を試みる
            orgIdStr = request.getAttribute(orgParam) != null ?
                request.getAttribute(orgParam).toString() : null;
        }

        Long boxTypeId = null;
        Long organizationId = null;

        if (boxTypeIdStr != null) {
            try {
                boxTypeId = Long.valueOf(boxTypeIdStr);
            } catch (NumberFormatException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "無効なボックスタイプIDです");
                return false;
            }
        }

        if (orgIdStr != null) {
            try {
                organizationId = Long.valueOf(orgIdStr);
            } catch (NumberFormatException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "無効な組織IDです");
                return false;
            }
        }

        // ボックスタイプIDと組織IDがある場合、そのボックスへの権限をチェック
        if (boxTypeId != null && organizationId != null) {
            Optional<BoxPermission> permissionOpt = boxPermissionRepository
                .findByUserIdAndBoxTypeIdAndOrganizationId(user.getId(), boxTypeId, organizationId);

            if (!permissionOpt.isPresent()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "このボックスへのアクセス権限がありません");
                return false;
            }

            BoxPermission permission = permissionOpt.get();

            // 有効期限チェック
            if (!permission.isValid()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "このボックスへのアクセス権限が期限切れです");
                return false;
            }

            // 権限レベルをチェック
            if (!hasRequiredPermission(permission, requiredPermission)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    String.format("このボックスへの%s権限がありません", requiredPermission));
                return false;
            }
        }

        return true;
    }

    /**
     * 必要な権限を持っているかチェック
     */
    private boolean hasRequiredPermission(BoxPermission permission, String requiredPermission) {
        switch (requiredPermission.toLowerCase()) {
            case "view":
            case "read":
                return permission.getCanView();
            case "post":
            case "write":
                return permission.getCanPost();
            case "reply":
                return permission.getCanReply();
            case "moderate":
            case "admin":
                return permission.getCanModerate();
            default:
                return false;
        }
    }
}
