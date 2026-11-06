package io.github.hejun.neutron.common.otlp.filter;

import io.github.hejun.neutron.common.otlp.constants.TraceIdHeader;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 将当前链路的 traceId 写入响应头
 *
 * @author HeJun
 */
public class TraceIdFilter extends OncePerRequestFilter {

    private final Tracer tracer;

    public TraceIdFilter(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
        throws ServletException, IOException {
        TraceContext context = tracer.currentTraceContext().context();
        if (context != null) {
            response.setHeader(TraceIdHeader.TRACE_ID, context.traceId());
        }
        filterChain.doFilter(request, response);
    }

}
