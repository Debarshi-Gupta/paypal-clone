package com.paypal.api_gateway.security;

import com.paypal.api_gateway.security.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtUtils jwtUtils;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.web.server.WebFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return chain.filter(exchange);
        }

        try {
            String token = authHeader.substring(7);

            String username = jwtUtils.extractUsername(token);
            Long userId = jwtUtils.extractUserId(token);
            String role = jwtUtils.extractRole(token);

            if (!jwtUtils.validateToken(token, username)) {
                log.warn("Invalid JWT token");
                return chain.filter(exchange);
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            username,
                            userId,
                            Collections.singletonList(new SimpleGrantedAuthority(role))
                    );

            exchange.getAttributes().put("authenticatedUserId", userId);

            log.info("Authenticated user={} for path={}", userId, path);

            return chain.filter(exchange)
                    .contextWrite(
                            ReactiveSecurityContextHolder.withAuthentication(authentication)
                    );

        } catch (Exception ex) {
            log.error("JWT validation failed", ex);
            return chain.filter(exchange);
        }
    }
}
