package com.bookstore.gateway.filter;

import com.bookstore.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * JWT全局认证过滤器
 * 从HTTP请求头 Authorization: Bearer <token> 中提取Token并验证
 * 白名单路径直接放行
 */
@Slf4j
@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    // 白名单：GET请求的公开路径（精确匹配或特定前缀）
    private static final List<String> WHITE_LIST_GET = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/product/books",     // 图书列表
            "/api/product/categories", // 分类列表
            "/api/product/books/rating", // 评分查询
            "/api/product/books/reviews", // 评论查询
            "/api/product/books/favorite" // 收藏状态查询
    );

    private static final List<String> WHITE_LIST_PREFIX = List.of(
            "/api/product/books/"  // 图书详情 GET /api/product/books/{id}
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethod() != null ? request.getMethod().name() : "GET";

        // 白名单路径直接放行（不校验Token，但GET请求可能需要用户信息用于获取个人评分/收藏状态）
        if (isWhitePath(path, method)) {
            // 尝试从Token获取用户信息（即使白名单也尽可能传递）
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    String token = authHeader.substring(7);
                    if (JwtUtil.validateToken(token)) {
                        Claims claims = JwtUtil.parseToken(token);
                        ServerHttpRequest modifiedRequest = request.mutate()
                                .header("X-User-Id", String.valueOf(claims.get("userId", Long.class)))
                                .header("X-Username", claims.getSubject())
                                .header("X-Role", claims.get("role", String.class))
                                .build();
                        return chain.filter(exchange.mutate().request(modifiedRequest).build());
                    }
                } catch (Exception ignored) {}
            }
            return chain.filter(exchange);
        }

        // 获取Authorization头
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorizedResponse(exchange, "未提供有效的认证Token");
        }

        String token = authHeader.substring(7);

        try {
            if (!JwtUtil.validateToken(token)) {
                return unauthorizedResponse(exchange, "Token无效或已过期");
            }

            Claims claims = JwtUtil.parseToken(token);

            // 将用户信息添加到请求头，传递给下游微服务
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Id", String.valueOf(claims.get("userId", Long.class)))
                    .header("X-Username", claims.getSubject())
                    .header("X-Role", claims.get("role", String.class))
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (Exception e) {
            log.warn("JWT验证失败: {}", e.getMessage());
            return unauthorizedResponse(exchange, "Token验证失败: " + e.getMessage());
        }
    }

    private boolean isWhitePath(String path, String method) {
        // 登录注册始终放行
        if (path.equals("/api/auth/login") || path.equals("/api/auth/register")) {
            return true;
        }
        // GET请求的公开路径
        if ("GET".equalsIgnoreCase(method)) {
            // 精确匹配
            if (WHITE_LIST_GET.stream().anyMatch(path::equals)) {
                return true;
            }
            // /api/product/books/{id} 及其子资源的GET请求
            if (path.matches("^/api/product/books/\\d+(/rating|/reviews|/favorite)?$")) {
                return true;
            }
        }
        return false;
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = String.format("{\"code\":401,\"message\":\"%s\",\"data\":null}", message);
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
