package com.suyos.apigateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

import java.util.UUID;

/**
 * Global filter for logging incoming requests and outgoing responses.
 */
@Component
public class LoggingGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(LoggingGlobalFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        String extractedTraceId = request.getHeaders().getFirst("traceparent");
        if (extractedTraceId != null && extractedTraceId.contains("-")) {
            String[] parts = extractedTraceId.split("-");
            if (parts.length >= 2) {
                extractedTraceId = parts[1];
            }
        }
        if (extractedTraceId == null || extractedTraceId.isEmpty()) {
            extractedTraceId = request.getHeaders().getFirst("x-trace-id");
        }
        if (extractedTraceId == null || extractedTraceId.isEmpty()) {
            extractedTraceId = UUID.randomUUID().toString();
        }
        
        final String traceId = extractedTraceId;
        final String spanId = UUID.randomUUID().toString().substring(0, 16);
        final long startTime = System.currentTimeMillis();
        
        return chain.filter(exchange)
            .contextWrite(Context.of("trace_id", traceId, "span_id", spanId))
            .doOnEach(signal -> {
                ContextView context = signal.getContextView();
                MDC.put("trace_id", context.getOrDefault("trace_id", "unknown"));
                MDC.put("span_id", context.getOrDefault("span_id", "unknown"));
            })
            .doFirst(() -> {
                MDC.put("trace_id", traceId);
                MDC.put("span_id", spanId);
                
                logger.info("event=gateway_request_received method={} path={} remote_addr={}",
                    request.getMethod(),
                    request.getPath().value(),
                    request.getRemoteAddress()
                );
                
                MDC.clear();
            })
            .then(Mono.fromRunnable(() -> {
                MDC.put("trace_id", traceId);
                MDC.put("span_id", spanId);
                
                ServerHttpResponse response = exchange.getResponse();
                long duration = System.currentTimeMillis() - startTime;
                
                logger.info("event=gateway_response_sent method={} path={} status={} duration_ms={}",
                    request.getMethod(),
                    request.getPath().value(),
                    response.getStatusCode(),
                    duration
                );
                
                MDC.clear();
            }));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
    
}