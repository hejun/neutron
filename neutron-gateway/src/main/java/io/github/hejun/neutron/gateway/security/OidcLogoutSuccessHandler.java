package io.github.hejun.neutron.gateway.security;

import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.web.server.DefaultServerRedirectStrategy;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * oidc 登出
 * <p>
 * 如果参数存在 <strong>continue</strong>, 则将 <strong>continue</strong> 编码为 <strong>state</strong> 参数, auth-server会回传回来, 然后通过这个回传路径重新跳转到前端
 *
 * @author HeJun
 */
@Component
public class OidcLogoutSuccessHandler implements ServerLogoutSuccessHandler {

    private final String matchParamKey = "continue";

    private final OidcClientInitiatedServerLogoutSuccessHandler oidcLogoutSuccessHandler;

    public OidcLogoutSuccessHandler(ReactiveClientRegistrationRepository clientRegistrationRepository) {
        DefaultServerRedirectStrategy serverRedirectStrategy = new DefaultServerRedirectStrategy() {

            @NonNull
            @Override
            public Mono<Void> sendRedirect(ServerWebExchange exchange, @NonNull URI location) {
                if (exchange.getRequest().getQueryParams().containsKey(matchParamKey)) {
                    String continueUrl = exchange.getRequest().getQueryParams().getFirst(matchParamKey);
                    if (StringUtils.hasText(continueUrl)) {
                        location = UriComponentsBuilder.fromUri(location)
                                .build()
                                .toUri();
                    }
                }
                return super.sendRedirect(exchange, location);
            }
        };

        oidcLogoutSuccessHandler =
                new OidcClientInitiatedServerLogoutSuccessHandler(clientRegistrationRepository);
        oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}");
        oidcLogoutSuccessHandler.setRedirectStrategy(serverRedirectStrategy);
    }

    @NonNull
    @Override
    public Mono<Void> onLogoutSuccess(@NonNull WebFilterExchange exchange, @NonNull Authentication authentication) {
        return oidcLogoutSuccessHandler.onLogoutSuccess(exchange, authentication);
    }
}
