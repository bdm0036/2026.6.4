package com.bookstore.gateway.config;

import com.bookstore.common.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 用户上下文过滤器 - 从请求头提取用户信息设置到ThreadLocal
 * 在JWT过滤器之后执行，order在JwtAuthFilter之后
 */
@Slf4j
@Component
public class UserContextFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        String username = exchange.getRequest().getHeaders().getFirst("X-Username");
        String role = exchange.getRequest().getHeaders().getFirst("X-Role");

        if (userId != null) {
            UserContext.setUserId(Long.valueOf(userId));
            UserContext.setUsername(username);
            UserContext.setRole(role);
        }

        return chain.filter(exchange).doFinally(signalType -> UserContext.clear());
    }

    @Override
    public int getOrder() {
        return -50;
    }
}
