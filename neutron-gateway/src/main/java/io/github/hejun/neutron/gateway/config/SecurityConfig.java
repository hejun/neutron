package io.github.hejun.neutron.gateway.config;

import io.github.hejun.neutron.gateway.security.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManagerResolver;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerReactiveAuthenticationManagerResolver;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.server.ServerWebExchange;

/**
 * 安全配置
 *
 * @author HeJun
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityConfig {

    @Bean
    @Order(SecurityFilterProperties.BASIC_AUTH_ORDER)
    public SecurityWebFilterChain webServerSecurityFilterChain(ServerHttpSecurity http,
                                                               ServerAuthenticationEntryPoint authenticationEntryPoint,
                                                               ServerOAuth2AuthorizationRequestResolver authorizationRequestResolver,
                                                               ServerLogoutSuccessHandler logoutSuccessHandler,
                                                               ServerAuthenticationFailureHandler authenticationFailureHandler,
                                                               ServerAccessDeniedHandler accessDeniedHandler,
                                                               SecurityProperties properties) {
        ReactiveAuthenticationManagerResolver<ServerWebExchange> authenticationManagerResolver =
            JwtIssuerReactiveAuthenticationManagerResolver.fromTrustedIssuers(issuer ->
                properties.getTrustedIssuers().contains(issuer)
            );

        http
            .authorizeExchange((exchange) ->
                exchange
                    .pathMatchers("/actuator/**", "/*/actuator/**", "/fs/public/**").permitAll()
                    .anyExchange().authenticated()
            )
            // 暂不处理 CSRF
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .oauth2Login(oauth2Login ->
                oauth2Login
                    .authorizationRequestResolver(authorizationRequestResolver)
            )
            // Web端使用
            .oauth2Client(Customizer.withDefaults())
            // OpenApi等直连端使用
            .oauth2ResourceServer(oauth2ResourceServer ->
                oauth2ResourceServer
                    .authenticationManagerResolver(authenticationManagerResolver)
                    .accessDeniedHandler(accessDeniedHandler)
                    .authenticationFailureHandler(authenticationFailureHandler)
            )
            .logout(logout ->
                logout
                    .requiresLogout(ServerWebExchangeMatchers.pathMatchers(HttpMethod.GET, "/logout"))
                    .logoutSuccessHandler(logoutSuccessHandler)
            )
            .exceptionHandling(exceptionHandling ->
                exceptionHandling
                    .authenticationEntryPoint(authenticationEntryPoint)
            );
        return http.build();
    }

}
