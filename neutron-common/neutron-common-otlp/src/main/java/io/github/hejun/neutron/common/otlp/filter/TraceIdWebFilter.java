package io.github.hejun.neutron.common.otlp.filter;

import io.github.hejun.neutron.common.otlp.constants.TraceIdHeader;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import org.jspecify.annotations.NonNull;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * 将当前链路的 traceId 写入响应头
 *
 * @author HeJun
 */
public class TraceIdWebFilter implements WebFilter, Ordered {

    private final Tracer tracer;

    public TraceIdWebFilter(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public @NonNull Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        TraceContext context = tracer.currentTraceContext().context();
        if (context != null) {
            exchange.getResponse().getHeaders().set(TraceIdHeader.TRACE_ID, context.traceId());
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }

}
