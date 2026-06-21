package io.github.hejun.neutron.fs.config;

import io.github.hejun.neutron.common.core.dto.Result;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 异常处理
 *
 * @author HeJun
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class ExceptionAdviceConfig {

    private final Tracer tracer;

    @ResponseStatus(code = HttpStatus.OK)
    @ExceptionHandler(Throwable.class)
    public Result<Void> handleException(Throwable throwable) {
        log.error("服务异常", throwable);
        String msg = "服务异常";
        TraceContext context = tracer.currentTraceContext().context();
        if (context != null) {
            String traceId = context.traceId();
            msg += ", 可通过追踪码: " + traceId + " 联系服务人员排查";
        }
        return Result.ERROR(500, msg);
    }

}
