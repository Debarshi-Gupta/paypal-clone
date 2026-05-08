package com.paypal.api_gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;

@Configuration
@Slf4j
public class RateLimiterConfig {

    @Bean
    public KeyResolver userKeyResolver() {

        return exchange -> {

            Object userId =
                    exchange.getAttributes().get("authenticatedUserId");

            if (userId != null) {

                log.debug("Rate limiting using userId={}", userId);

                return Mono.just(userId.toString());
            }

            String clientIp =
                    exchange.getRequest()
                            .getRemoteAddress()
                            .getAddress()
                            .getHostAddress();

            log.debug("Rate limiting using clientIp={}", clientIp);

            return Mono.just(clientIp);
        };
    }
}
