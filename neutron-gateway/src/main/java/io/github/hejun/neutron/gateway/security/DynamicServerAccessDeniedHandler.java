package io.github.hejun.neutron.gateway.security;

import io.github.hejun.neutron.common.core.dto.Result;
import org.jspecify.annotations.NonNull;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.server.resource.BearerTokenErrorCodes;
import org.springframework.security.oauth2.server.resource.web.access.server.BearerTokenServerAccessDeniedHandler;
import org.springframework.security.web.server.util.matcher.MediaTypeServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import tools.jackson.databind.json.JsonMapper;

import java.util.Collections;

/**
 * 根据 <strong>Content-Type</strong> 判断返回响应类型
 *
 * @author HeJun
 */
@Component
public class DynamicServerAccessDeniedHandler extends BearerTokenServerAccessDeniedHandler {

    private final JsonMapper mapper = JsonMapper.builder().build();
    private final ServerWebExchangeMatcher jsonMatcher;

    public DynamicServerAccessDeniedHandler() {
        MediaTypeServerWebExchangeMatcher jsonMediaMatcher = new MediaTypeServerWebExchangeMatcher(MediaType.APPLICATION_JSON);
        jsonMediaMatcher.setIgnoredMediaTypes(Collections.singleton(MediaType.ALL));
        ServerWebExchangeMatcher xhrMatcher = (exchange) -> {
            HttpHeaders headers = exchange.getRequest().getHeaders();
            if (headers.getOrEmpty("X-Requested-With").contains("XMLHttpRequest")
                || headers.getAccept().contains(MediaType.APPLICATION_JSON)) {
                return ServerWebExchangeMatcher.MatchResult.match();
            }
            return ServerWebExchangeMatcher.MatchResult.notMatch();
        };
        this.jsonMatcher = new OrServerWebExchangeMatcher(jsonMediaMatcher, xhrMatcher);
    }

    @NonNull
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException deniedException) {
        return jsonMatcher.matches(exchange)
            .flatMap(matchResult->{
                if (matchResult.isMatch()) {
                    ServerHttpResponse response = exchange.getResponse();
                    response.setStatusCode(HttpStatus.OK);
                    response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

                    Result<Void> result = Result.ERROR(HttpStatus.FORBIDDEN.value(), BearerTokenErrorCodes.INSUFFICIENT_SCOPE);
                    DataBuffer dataBuffer = response.bufferFactory().wrap(mapper.writeValueAsBytes(result));
                    return response.writeWith(Mono.just(dataBuffer));
                } else {
                    return super.handle(exchange, deniedException);
                }
            });
    }

}
