package io.github.hejun.neutron.common.server.resource.config;

import io.github.hejun.neutron.common.server.resource.properties.SecurityProperties;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.ReactiveAuthenticationManagerResolver;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerReactiveAuthenticationManagerResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;

/**
 * 资源服务安全配置
 *
 * @author HeJun
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(SecurityProperties.class)
public class ResourceSecurityConfig {

    @Configuration
    @EnableWebSecurity
    @ConditionalOnClass(HttpServletResponse.class)
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    static class ServletSecurityFilterChainConfig {

        @Bean
        @Order(SecurityFilterProperties.BASIC_AUTH_ORDER)
        public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http,
                                                              SecurityProperties properties) {
            JwtIssuerAuthenticationManagerResolver authenticationManagerResolver = JwtIssuerAuthenticationManagerResolver
                .fromTrustedIssuers(issuer ->
                    properties.getTrustedIssuers().contains(issuer)
                );

            http
                .authorizeHttpRequests((authorize) ->
                    authorize
                        .requestMatchers("/actuator/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer((resourceServer) ->
                    resourceServer
                        .authenticationManagerResolver(authenticationManagerResolver)
                );
            return http.build();
        }

    }

    @Configuration
    @EnableWebFluxSecurity
    @ConditionalOnClass(WebFilter.class)
    @ConditionalOnMissingBean(SecurityWebFilterChain.class)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    static class ReactiveSecurityFilterChainConfig {

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
                        .pathMatchers("/actuator/**", "/*/actuator/**").permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer((resourceServer) ->
                    resourceServer
                        .authenticationManagerResolver(authenticationManagerResolver)
                );
            return http.build();
        }

    }

}
