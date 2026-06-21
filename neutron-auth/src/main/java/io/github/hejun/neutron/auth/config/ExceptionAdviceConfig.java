package io.github.hejun.neutron.auth.config;

import io.github.hejun.neutron.auth.exception.OccupiedException;
import io.github.hejun.neutron.common.core.dto.Result;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    @ExceptionHandler({OccupiedException.class})
    public Result<Void> handleTenantRepeatException(OccupiedException exception) {
        return Result.ERROR(400, exception.getMessage());
    }

    @ResponseStatus(code = HttpStatus.OK)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        List<ObjectError> allErrors = exception.getBindingResult().getAllErrors();
        String message = allErrors.stream()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .collect(Collectors.joining(";"));
        return Result.ERROR(400, message);
    }

    @ResponseStatus(code = HttpStatus.OK)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<Map<String, String>> handleMethodArgumentNotValidException(HttpRequestMethodNotSupportedException exception) {
        return Result.ERROR(400, "请求方法不支持", Map.of("requestMethod", exception.getMethod()));
    }

    @ResponseStatus(code = HttpStatus.OK)
    @ExceptionHandler(NoResourceFoundException.class)
    public Result<Map<String, String>> handleMethodArgumentNotValidException(NoResourceFoundException exception) {
        return Result.ERROR(404, "请求资源未找到", Map.of("resourcePath", exception.getResourcePath()));
    }

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
