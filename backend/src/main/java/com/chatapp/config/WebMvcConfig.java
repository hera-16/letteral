package com.chatapp.config;

import com.chatapp.interceptor.BoxAccessInterceptor;
import com.chatapp.interceptor.RoleAuthorizationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC設定
 * インターセプターの登録を行う
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private RoleAuthorizationInterceptor roleAuthorizationInterceptor;

    @Autowired
    private BoxAccessInterceptor boxAccessInterceptor;

    @Override
    public void addInterceptors(@org.springframework.lang.NonNull InterceptorRegistry registry) {
        // ロール認可インターセプターを登録
        registry.addInterceptor(roleAuthorizationInterceptor)
            .addPathPatterns("/api/**")
            .excludePathPatterns(
                "/api/auth/**",
                "/api/public/**",
                "/actuator/**"
            );

        // ボックスアクセスインターセプターを登録
        registry.addInterceptor(boxAccessInterceptor)
            .addPathPatterns("/api/**")
            .excludePathPatterns(
                "/api/auth/**",
                "/api/public/**",
                "/actuator/**"
            );
    }
}
