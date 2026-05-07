package io.github.hejun.neutron.gateway.security;

import io.github.hejun.neutron.common.core.dto.Result;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationEntryPoint;
import org.springframework.security.web.server.util.matcher.MediaTypeServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 多 client 应用根据 <strong>HOST</strong> 动态适配,并根据 <strong>Content-Type</strong> 判断返回响应类型
 * <p>
 * 这个配置应用后, registration 的 redirect-uri 不能写通配符 {baseUrl}, 因为需要根据 host 去匹配对应的 client
 *
 * @author HeJun
 */
@Slf4j
@Component
public class DynamicServerAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();
    private final Map<String, String> loginClientMapping = new HashMap<>();
    private final ServerWebExchangeMatcher jsonMatcher;

    public DynamicServerAuthenticationEntryPoint(InMemoryReactiveClientRegistrationRepository clientRegistrationRepository) {
        clientRegistrationRepository.forEach(registration -> {
            String trustedHost = UriComponentsBuilder.fromUriString(registration.getRedirectUri())
                .replacePath(null)
                .build()
                .toUriString();
            loginClientMapping.put(trustedHost, "/oauth2/authorization/" + registration.getRegistrationId());
        });

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
    public Mono<Void> commence(ServerWebExchange exchange, @NonNull AuthenticationException ex) {
        URI requestUri = exchange.getRequest().getURI();
        String requestHost = UriComponentsBuilder.fromUri(requestUri)
            .replacePath(null)
            .build()
            .toUriString();

        String loginPage = loginClientMapping.getOrDefault(requestHost, "/login");

        return jsonMatcher.matches(exchange)
            .flatMap(matchResult -> {
                if (matchResult.isMatch()) {
                    Result<Map<String, String>> result = Result
                        .ERROR(HttpStatus.UNAUTHORIZED.value(), ex.getMessage(), Map.of("location", loginPage));
                    ServerHttpResponse response = exchange.getResponse();
                    response.setStatusCode(HttpStatus.OK);
                    response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                    DataBuffer dataBuffer = response.bufferFactory().wrap(jsonMapper.writeValueAsBytes(result));
                    return response.writeWith(Mono.just(dataBuffer));
                } else {
                    ServerAuthenticationEntryPoint authenticationEntryPoint = new RedirectServerAuthenticationEntryPoint(loginPage);
                    return authenticationEntryPoint.commence(exchange, ex);
                }
            });
    }

}
