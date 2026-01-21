package com.suyos.apigateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Global filter for logging incoming requests and outgoing responses.
 */
@Component
public class TraceLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(TraceLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        long start = System.currentTimeMillis();

        ServerHttpRequest request = exchange.getRequest();

        log.info("gateway_request method={} path={}",
            request.getMethod(),
            request.getPath().value()
        );

        return chain.filter(exchange)
            .doFinally(signal -> {
                long duration = System.currentTimeMillis() - start;
                log.info("gateway_response status={} duration_ms={}",
                    exchange.getResponse().getStatusCode(),
                    duration
                );
            });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}