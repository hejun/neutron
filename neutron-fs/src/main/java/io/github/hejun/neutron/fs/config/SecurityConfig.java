package io.github.hejun.neutron.fs.config;

import io.github.hejun.neutron.common.server.resource.properties.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.ReactiveAuthenticationManagerResolver;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerReactiveAuthenticationManagerResolver;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.server.ServerWebExchange;

/**
 * 安全配置
 *
 * @author HeJun
 */
@EnableWebFluxSecurity
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityConfig {

    @Bean
    @Order(SecurityFilterProperties.BASIC_AUTH_ORDER)
    public SecurityWebFilterChain defaultSecurityWebFilterChain(ServerHttpSecurity http,
                                                                SecurityProperties properties) {
        ReactiveAuthenticationManagerResolver<ServerWebExchange> authenticationManagerResolver =
            JwtIssuerReactiveAuthenticationManagerResolver.fromTrustedIssuers(issuer ->
                properties.getTrustedIssuers().contains(issuer)
            );

        http
            .requestCache(ServerHttpSecurity.RequestCacheSpec::disable)
            .authorizeExchange((exchange) ->
                exchange
                    .pathMatchers("/actuator/**", "/*/actuator/**", "/public/**").permitAll()
                    .anyExchange().authenticated()
            )
            .oauth2ResourceServer((resourceServer) ->
                resourceServer
                    .authenticationManagerResolver(authenticationManagerResolver)
            );
        return http.build();
    }

}
