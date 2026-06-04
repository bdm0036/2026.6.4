package com.bookstore.common.filter;

import com.bookstore.common.utils.UserContext;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 用户上下文过滤器 - 从请求头提取X-User-Id等信息设置到ThreadLocal
 * 每个微服务都需要此过滤器来接收Gateway传递的用户信息
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ServiceUserContextFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpRequest) {
            String userId = httpRequest.getHeader("X-User-Id");
            String username = httpRequest.getHeader("X-Username");
            String role = httpRequest.getHeader("X-Role");

            if (userId != null) {
                UserContext.setUserId(Long.valueOf(userId));
                UserContext.setUsername(username);
                UserContext.setRole(role);
                log.debug("UserContext set: userId={}, username={}, role={}", userId, username, role);
            }
        }

        try {
            chain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }
}
