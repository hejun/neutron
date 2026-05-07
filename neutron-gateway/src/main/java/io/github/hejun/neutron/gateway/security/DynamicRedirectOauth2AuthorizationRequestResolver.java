package io.github.hejun.neutron.gateway.security;

import org.jspecify.annotations.NonNull;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.server.DefaultServerOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.security.web.server.savedrequest.WebSessionServerRequestCache;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * 在跳转到 /oauth2/authorization/{registrationId} 时
 * <p>
 * 如果参数存在 <strong>continue</strong>, 则替换原有路径为 <strong>continue</strong> 指向的路径
 *
 * @author HeJun
 */
@Component
public class DynamicRedirectOauth2AuthorizationRequestResolver extends DefaultServerOAuth2AuthorizationRequestResolver {

    // 需要与 WebSessionServerRequestCache 里的 sessionAttrName 一致
    private final String sessionAttrName = "SPRING_SECURITY_SAVED_REQUEST";
    private final String matchParamKey = "continue";

    private final ServerWebExchangeMatcher oauth2AuthorizationRequest =
        new PathPatternParserServerWebExchangeMatcher("/oauth2/authorization/{registrationId}");

    // 重写 ServerRequestCache, 替换跳转路径为 continue 参数指向的路径
    private final ServerRequestCache requestCache = new WebSessionServerRequestCache() {

        @NonNull
        @Override
        public Mono<Void> saveRequest(@NonNull ServerWebExchange exchange) {
            return super.saveRequest(exchange)
                .then(Mono.defer(() -> {
                    if (exchange.getRequest().getQueryParams().containsKey(matchParamKey)) {
                        String continueUrl = exchange.getRequest().getQueryParams().getFirst(matchParamKey);
                        if (StringUtils.hasText(continueUrl)) {
                            String decoderContinueUrl = URLDecoder.decode(continueUrl, StandardCharsets.UTF_8);
                            return exchange.getSession()
                                .map(WebSession::getAttributes)
                                .doOnNext(attr -> attr.put(sessionAttrName, decoderContinueUrl))
                                .then();
                        }
                    }
                    return Mono.empty();
                }));
        }
    };

    public DynamicRedirectOauth2AuthorizationRequestResolver(ReactiveClientRegistrationRepository clientRegistrationRepository) {
        super(clientRegistrationRepository);
    }

    @Override
    public Mono<OAuth2AuthorizationRequest> resolve(ServerWebExchange exchange, String clientRegistrationId) {
        return oauth2AuthorizationRequest
            .matches(exchange)
            .filter(ServerWebExchangeMatcher.MatchResult::isMatch)
            .filter((matchResult) ->
                exchange
                    .getRequest()
                    .getQueryParams()
                    .containsKey(matchParamKey)
            )
            .flatMap(matchResult ->
                requestCache
                    .saveRequest(exchange)
                    .then(super.resolve(exchange, clientRegistrationId)))
            .switchIfEmpty(super.resolve(exchange, clientRegistrationId));
    }

}
