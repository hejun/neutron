package io.github.hejun.neutron.common.otlp.autoconfigure;

import io.github.hejun.neutron.common.otlp.filter.TraceIdWebFilter;
import io.micrometer.tracing.Tracer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.micrometer.tracing.autoconfigure.MicrometerTracingAutoConfiguration;
import org.springframework.boot.micrometer.tracing.opentelemetry.autoconfigure.OpenTelemetryTracingAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

/**
 * Reactive 应用的 traceId 响应头配置
 *
 * @author HeJun
 */
@AutoConfiguration(after = {MicrometerTracingAutoConfiguration.class, OpenTelemetryTracingAutoConfiguration.class})
@ConditionalOnClass({Tracer.class, WebFilter.class, Mono.class})
@ConditionalOnBean(Tracer.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class TraceIdReactiveAutoConfiguration {

    @Bean
    public TraceIdWebFilter traceIdWebFilter(Tracer tracer) {
        return new TraceIdWebFilter(tracer);
    }

}
