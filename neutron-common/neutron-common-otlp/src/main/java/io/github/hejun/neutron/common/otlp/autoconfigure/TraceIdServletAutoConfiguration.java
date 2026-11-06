package io.github.hejun.neutron.common.otlp.autoconfigure;

import io.github.hejun.neutron.common.otlp.filter.TraceIdFilter;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.micrometer.tracing.autoconfigure.MicrometerTracingAutoConfiguration;
import org.springframework.boot.micrometer.tracing.opentelemetry.autoconfigure.OpenTelemetryTracingAutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

/**
 * Servlet 应用的 traceId 响应头配置
 *
 * @author HeJun
 */
@AutoConfiguration(after = {MicrometerTracingAutoConfiguration.class, OpenTelemetryTracingAutoConfiguration.class})
@ConditionalOnClass({Tracer.class, HttpServletResponse.class})
@ConditionalOnBean(Tracer.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class TraceIdServletAutoConfiguration {

    @Bean
    public FilterRegistrationBean<@NonNull TraceIdFilter> traceIdFilter(Tracer tracer) {
        FilterRegistrationBean<@NonNull TraceIdFilter> registration = new FilterRegistrationBean<>(new TraceIdFilter(tracer));
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        return registration;
    }

}
