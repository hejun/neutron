package io.github.hejun.neutron.gateway.config;

import io.github.hejun.neutron.common.core.dto.Result;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * 通用路由配置
 *
 * @author HeJun
 */
@Configuration(proxyBeanMethods = false)
public class RouterConfig {

    @Bean
    public RouterFunction<@NonNull ServerResponse> oidcLogoutRedirectRouter(ReactiveClientRegistrationRepository reactiveClientRegistrationRepository) {
        return RouterFunctions.route()
            // 定义用户信息端点
            .GET("/userinfo", request ->
                request
                    .principal()
                    .cast(OAuth2AuthenticationToken.class)
                    .flatMap(token -> {
                        Map<String, Object> claims;
                        OAuth2User principal = token.getPrincipal();
                        if (principal instanceof OidcUser oidcUser) {
                            claims = new HashMap<>(oidcUser.getUserInfo().getClaims());
                        } else if (principal != null) {
                            claims = new HashMap<>(principal.getAttributes());
                        } else {
                            return Mono.empty();
                        }

                        // 增加 aud_name 用于标识客户端
                        Result<Map<String, Object>> successResult = Result.SUCCESS(claims);
                        return reactiveClientRegistrationRepository.findByRegistrationId(token.getAuthorizedClientRegistrationId())
                            .flatMap(clientRegistration -> {
                                claims.putIfAbsent("aud", clientRegistration.getClientId());
                                claims.putIfAbsent("aud_name", clientRegistration.getClientName());
                                return ServerResponse.ok()
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(successResult);
                            })
                            .switchIfEmpty(
                                ServerResponse.ok()
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(successResult)
                            );
                    })
            )
            .build();
    }

}
