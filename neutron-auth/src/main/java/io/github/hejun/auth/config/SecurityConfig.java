package io.github.hejun.auth.config;

import io.github.hejun.auth.entity.Client;
import io.github.hejun.auth.security.AuthorizationServerMultiIssuerConfigurer;
import io.github.hejun.auth.service.IClientService;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcUserInfoAuthenticationContext;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * 安全配置
 *
 * @author HeJun
 */
@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        if (passwordEncoder instanceof DelegatingPasswordEncoder delegatingPasswordEncoder) {
            delegatingPasswordEncoder.setDefaultPasswordEncoderForMatches(new BCryptPasswordEncoder());
        }
        return passwordEncoder;
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http, IClientService clientService) {
        http
            .oauth2AuthorizationServer((authorizationServer) -> {
                http
                    .securityMatcher(authorizationServer.getEndpointsMatcher());
                authorizationServer
                    .authorizationEndpoint(authorizationEndpoint ->
                        authorizationEndpoint.consentPage("/consent")
                    )
                    .oidc(oidc -> oidc
                        .userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint
                            .userInfoMapper(this.userInfoMapper.apply(clientService))
                        )
                    );
            })
            .authorizeHttpRequests((authorize) ->
                authorize
                    .anyRequest().authenticated()
            )
            .exceptionHandling((exceptions) -> exceptions
                .defaultAuthenticationEntryPointFor(
                    new LoginUrlAuthenticationEntryPoint("/login"),
                    this.createRequestMatcher()
                )
            );
        return http.build();
    }

    private final Function<IClientService, Function<OidcUserInfoAuthenticationContext, OidcUserInfo>> userInfoMapper = clientService -> context -> {
        OAuth2Authorization authorization = context.getAuthorization();
        Client client = clientService.findById(Long.valueOf(authorization.getRegisteredClientId()));
        return new OidcUserInfo(Map.of(
            "aud", Optional.ofNullable(client.getClientId()).orElse(""),
            "aud_name", Optional.ofNullable(client.getClientName()).orElse(""),
            "aud_logo", Optional.ofNullable(client.getLogoUrl()).orElse(""),
            "sub", authorization.getPrincipalName()
        ));
    };

    private RequestMatcher createRequestMatcher() {
        MediaTypeRequestMatcher requestMatcher = new MediaTypeRequestMatcher(MediaType.TEXT_HTML);
        requestMatcher.setIgnoredMediaTypes(Set.of(MediaType.ALL));
        return requestMatcher;
    }

    @Bean
    @Order(SecurityFilterProperties.BASIC_AUTH_ORDER)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) {
        AuthorizationServerMultiIssuerConfigurer multiIssuerConfigurer = new AuthorizationServerMultiIssuerConfigurer();

        http
            .authorizeHttpRequests((authorize) -> authorize
                .requestMatchers(
                    "/favicon.ico", "/robots.txt",
                    "/assets/**", "/.well-known/**",
                    "/actuator/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(formLogin ->
                formLogin
                    .loginPage("/login").permitAll()
                    .loginProcessingUrl("/login/account")
            )
            .oauth2ResourceServer((resourceServer) ->
                resourceServer
                    .jwt(Customizer.withDefaults())
            )
            .with(multiIssuerConfigurer, Customizer.withDefaults());
        return http.build();
    }

}
