package com.krolak.service.auth;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiKeyAuthFilter implements Filter {

    @Value("${app.security.api-key}")
    private String apiKey;

    private final List<String> protectedPaths = List.of("/api/v1/github");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();

        if (isProtectedPath(path)) {
            String providedApiKey = httpRequest.getHeader("X-API-Key");
            if (providedApiKey == null || !providedApiKey.equals(apiKey)) {
                httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid API Key");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private boolean isProtectedPath(String path) {
        return protectedPaths.stream().anyMatch(path::startsWith);
    }
}