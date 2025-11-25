package com.chatapp.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * レート制限設定クラス
 *
 * IPアドレスベースのレート制限を実装
 * 本番環境では、RedisベースまたはAWS WAFの使用を推奨
 */
@Configuration
@ConditionalOnProperty(name = "rate.limit.enabled", havingValue = "true", matchIfMissing = false)
public class RateLimitConfig {

    /**
     * レート制限フィルター
     */
    @Bean
    public RateLimitFilter rateLimitFilter() {
        return new RateLimitFilter();
    }

    /**
     * レート制限フィルター実装
     */
    public static class RateLimitFilter extends OncePerRequestFilter {

        // IPアドレスごとのリクエストカウンタ
        private final ConcurrentHashMap<String, RequestCounter> requestCounts = new ConcurrentHashMap<>();

        // レート制限: 100リクエスト/分
        private static final int MAX_REQUESTS_PER_MINUTE = 100;
        private static final long WINDOW_SIZE_MS = 60_000; // 1分

        @Override
        protected void doFilterInternal(
                HttpServletRequest request,
                HttpServletResponse response,
                FilterChain filterChain) throws ServletException, IOException {

            String clientIp = getClientIp(request);

            // リクエストカウンタを取得または作成
            RequestCounter counter = requestCounts.computeIfAbsent(clientIp, k -> new RequestCounter());

            // 期限切れカウンタのクリーンアップ（定期的に）
            cleanupExpiredCounters();

            // レート制限チェック
            if (counter.increment() > MAX_REQUESTS_PER_MINUTE) {
                response.setStatus(429); // 429 Too Many Requests
                response.setContentType("application/json");
                response.getWriter().write(
                    "{\"error\":\"Too many requests\",\"message\":\"Rate limit exceeded. Please try again later.\"}"
                );
                return;
            }

            filterChain.doFilter(request, response);
        }

        /**
         * クライアントIPアドレスを取得
         * プロキシ経由の場合はX-Forwarded-Forヘッダーから取得
         */
        private String getClientIp(HttpServletRequest request) {
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        }

        /**
         * 期限切れカウンタのクリーンアップ
         */
        private void cleanupExpiredCounters() {
            long now = System.currentTimeMillis();
            requestCounts.entrySet().removeIf(entry ->
                now - entry.getValue().getStartTime() > WINDOW_SIZE_MS * 2
            );
        }

        /**
         * リクエストカウンタ
         */
        private static class RequestCounter {
            private final AtomicInteger count = new AtomicInteger(0);
            private volatile long startTime = System.currentTimeMillis();

            public int increment() {
                long now = System.currentTimeMillis();

                // ウィンドウ期間を超えた場合、カウンタをリセット
                if (now - startTime > WINDOW_SIZE_MS) {
                    synchronized (this) {
                        if (now - startTime > WINDOW_SIZE_MS) {
                            count.set(0);
                            startTime = now;
                        }
                    }
                }

                return count.incrementAndGet();
            }

            public long getStartTime() {
                return startTime;
            }
        }
    }
}
