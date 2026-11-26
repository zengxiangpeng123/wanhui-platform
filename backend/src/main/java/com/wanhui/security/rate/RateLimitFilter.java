package com.wanhui.security.rate;

import com.wanhui.config.SecurityErrorWriter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * 对登录、注册和发表评论做限流。
 * 超限返回 429 RATE_LIMITED，响应结构与其他错误保持一致。
 */
public class RateLimitFilter extends OncePerRequestFilter {
    private static final Pattern COMMENT_PATH = Pattern.compile("^/api/v1/forum/posts/\\d+/comments$");

    private final boolean enabled;
    private final InMemoryRateLimiter loginLimiter;
    private final InMemoryRateLimiter registerLimiter;
    private final InMemoryRateLimiter commentLimiter;
    private final SecurityErrorWriter errorWriter;

    public RateLimitFilter(
            boolean enabled,
            InMemoryRateLimiter loginLimiter,
            InMemoryRateLimiter registerLimiter,
            InMemoryRateLimiter commentLimiter,
            SecurityErrorWriter errorWriter
    ) {
        this.enabled = enabled;
        this.loginLimiter = loginLimiter;
        this.registerLimiter = registerLimiter;
        this.commentLimiter = commentLimiter;
        this.errorWriter = errorWriter;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (enabled) {
            RateLimitScope scope = resolveScope(request);
            if (scope != null) {
                InMemoryRateLimiter limiter = limiterFor(scope);
                String key = scope.name() + ":" + clientKey(request);
                if (!limiter.tryAcquire(key)) {
                    errorWriter.write(
                            response,
                            HttpStatus.TOO_MANY_REQUESTS,
                            "RATE_LIMITED",
                            "请求过于频繁，请稍后再试"
                    );
                    return;
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private InMemoryRateLimiter limiterFor(RateLimitScope scope) {
        return switch (scope) {
            case LOGIN -> loginLimiter;
            case REGISTER -> registerLimiter;
            case COMMENT -> commentLimiter;
        };
    }

    private RateLimitScope resolveScope(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return null;
        }
        String path = request.getRequestURI();
        if ("/api/v1/auth/login".equals(path)) {
            return RateLimitScope.LOGIN;
        }
        if ("/api/v1/auth/register".equals(path)) {
            return RateLimitScope.REGISTER;
        }
        if (COMMENT_PATH.matcher(path).matches()) {
            return RateLimitScope.COMMENT;
        }
        return null;
    }

    private String clientKey(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            int comma = forwarded.indexOf(',');
            String first = comma > 0 ? forwarded.substring(0, comma) : forwarded;
            return first.trim();
        }
        return request.getRemoteAddr();
    }

    private enum RateLimitScope {
        LOGIN, REGISTER, COMMENT
    }
}
