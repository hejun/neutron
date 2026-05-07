package io.github.hejun.neutron.gateway.security;

import io.github.hejun.neutron.common.core.dto.Result;
import org.jspecify.annotations.NonNull;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.server.resource.BearerTokenError;
import org.springframework.security.oauth2.server.resource.web.server.BearerTokenServerAuthenticationEntryPoint;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationEntryPointFailureHandler;
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
public class DynamicServerAuthenticationFailureHandler extends ServerAuthenticationEntryPointFailureHandler {

    private final JsonMapper mapper = JsonMapper.builder().build();
    private final ServerWebExchangeMatcher jsonMatcher;

    public DynamicServerAuthenticationFailureHandler() {
        super(new BearerTokenServerAuthenticationEntryPoint());

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
    public Mono<Void> onAuthenticationFailure(WebFilterExchange webFilterExchange, @NonNull AuthenticationException exception) {
        ServerWebExchange exchange = webFilterExchange.getExchange();
        return jsonMatcher.matches(exchange)
            .flatMap(matchResult -> {
                if (matchResult.isMatch()) {
                    ServerHttpResponse response = exchange.getResponse();
                    response.setStatusCode(HttpStatus.OK);
                    response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

                    Result<Void> result = getErrorResult(exception);
                    DataBuffer dataBuffer = response.bufferFactory().wrap(mapper.writeValueAsBytes(result));

                    return response.writeWith(Mono.just(dataBuffer));
                } else {
                    return super.onAuthenticationFailure(webFilterExchange, exception);
                }
            });
    }

    private Result<Void> getErrorResult(AuthenticationException authException) {
        if (authException instanceof OAuth2AuthenticationException oAuth2AuthenticationException) {
            OAuth2Error error = oAuth2AuthenticationException.getError();
            if (error instanceof BearerTokenError bearerTokenError) {
                return Result.ERROR(bearerTokenError.getHttpStatus().value(), bearerTokenError.getDescription());
            }
            return Result.ERROR(HttpStatus.UNAUTHORIZED.value(), error.getDescription());
        }
        return Result.ERROR(HttpStatus.UNAUTHORIZED.value(), authException.getMessage());
    }

}
