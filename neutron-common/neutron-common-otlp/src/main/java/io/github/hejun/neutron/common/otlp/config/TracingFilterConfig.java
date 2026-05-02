package io.github.hejun.neutron.common.otlp.config;

import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.micrometer.tracing.autoconfigure.MicrometerTracingAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.task.support.ContextPropagatingTaskDecorator;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.io.IOException;

/**
 * 日志追踪输出配置
 *
 * @author HeJun
 */
@Configuration
@ConditionalOnBean(Tracer.class)
@AutoConfigureAfter({MicrometerTracingAutoConfiguration.class})
public class TracingFilterConfig {

    private static final String HEADER_TRACEPARENT = "Traceparent";

    @Bean
    public ContextPropagatingTaskDecorator contextPropagatingTaskDecorator() {
        return new ContextPropagatingTaskDecorator();
    }

    @Configuration
    @ConditionalOnClass(HttpServletResponse.class)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    static class ServletTracingFilterConfig {

        @Bean
        @ConditionalOnBean(Tracer.class)
        @Order(Ordered.HIGHEST_PRECEDENCE)
        public OncePerRequestFilter tracingPerRequestFilter(Tracer tracer) {
            return new OncePerRequestFilter() {

                @Override
                protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                                @NonNull FilterChain filterChain) throws ServletException, IOException {
                    if (request.getHeader(HEADER_TRACEPARENT) == null) {
                        TraceContext context = tracer.currentTraceContext().context();
                        if (context != null) {
                            response.addHeader(HEADER_TRACEPARENT, context.traceId());
                        }
                    }
                    filterChain.doFilter(request, response);
                }

            };
        }

    }

    @Configuration
    @ConditionalOnClass(WebFilter.class)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    static class ReactiveTracingFilterConfig {

        @Bean
        @ConditionalOnBean(Tracer.class)
        @Order(Ordered.HIGHEST_PRECEDENCE)
        public WebFilter tracingWebFilter(Tracer tracer) {
            return new WebFilter() {

                @NonNull
                @Override
                public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
                    if (!exchange.getResponse().getHeaders().containsHeader(HEADER_TRACEPARENT)) {
                        TraceContext context = tracer.currentTraceContext().context();
                        if (context != null) {
                            exchange.getResponse().beforeCommit(() -> {
                                exchange.getResponse().getHeaders().add(HEADER_TRACEPARENT, context.traceId());
                                return Mono.empty();
                            });
                        }
                    }
                    return chain.filter(exchange);
                }

            };
        }

    }

}
