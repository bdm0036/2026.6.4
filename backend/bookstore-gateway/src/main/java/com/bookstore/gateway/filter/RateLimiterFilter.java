package com.bookstore.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Component
public class RateLimiterFilter implements GlobalFilter, Ordered {

    private final ReactiveStringRedisTemplate redisTemplate;
    private static final int GLOBAL_RATE = 100;
    private static final int LOGIN_RATE = 5;

    public RateLimiterFilter(@org.springframework.beans.factory.annotation.Autowired(required = false)
                             ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private boolean hasRedis() { return redisTemplate != null; }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String ip = getClientIp(exchange);
        String path = exchange.getRequest().getURI().getPath();

        String key;
        int limit;
        Duration window;

        if (path.contains("/auth/login")) {
            key = "rate:login:" + ip;
            limit = LOGIN_RATE;
            window = Duration.ofMinutes(1);
        } else {
            key = "rate:global:" + ip;
            limit = GLOBAL_RATE;
            window = Duration.ofSeconds(1);
        }

        if (!hasRedis()) return chain.filter(exchange);
        return redisTemplate.opsForValue().increment(key)
            .flatMap(count -> {
                if (count == 1) {
                    redisTemplate.expire(key, window).subscribe();
                }
                if (count > limit) {
                    log.warn("Rate limit exceeded: key={} count={}", key, count);
                    exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                    return exchange.getResponse().setComplete();
                }
                return chain.filter(exchange);
            })
            .onErrorResume(e -> {
                log.warn("Rate limiter Redis error, passing through: {}", e.getMessage());
                return chain.filter(exchange);
            });
    }

    private String getClientIp(ServerWebExchange exchange) {
        String xff = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) return xff.split(",")[0].trim();
        var remote = exchange.getRequest().getRemoteAddress();
        return remote != null ? remote.getAddress().getHostAddress() : "unknown";
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
