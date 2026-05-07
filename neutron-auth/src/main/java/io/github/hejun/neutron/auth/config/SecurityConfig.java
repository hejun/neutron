package io.github.hejun.neutron.auth.config;

import io.github.hejun.neutron.auth.entity.User;
import io.github.hejun.neutron.auth.properties.SecurityProperties;
import io.github.hejun.neutron.auth.service.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

import java.util.Objects;
import java.util.Optional;

/**
 * 安全配置
 *
 * @author HeJun
 */
@EnableWebSecurity
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(SecurityProperties.class)
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
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) {
        http
            .oauth2AuthorizationServer((authorizationServer) -> {
                http
                    .securityMatcher(authorizationServer.getEndpointsMatcher());

                authorizationServer
                    .authorizationEndpoint(authorizationEndpoint ->
                        authorizationEndpoint.consentPage("/consent")
                    )
                    .oidc(Customizer.withDefaults());
            })
            .authorizeHttpRequests((authorize) ->
                authorize
                    .anyRequest().authenticated()
            )
            .exceptionHandling((exceptions) ->
                exceptions
                    .defaultAuthenticationEntryPointFor(
                        new LoginUrlAuthenticationEntryPoint("/login"),
                        new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                    )
            );
        return http.build();
    }

    @Bean
    @Order(SecurityFilterProperties.BASIC_AUTH_ORDER)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http,
                                                          SecurityProperties properties) {
        AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver =
            JwtIssuerAuthenticationManagerResolver.fromTrustedIssuers(issuer ->
                properties.getTrustedIssuers().contains(issuer)
            );

        http
            .authorizeHttpRequests((authorize) ->
                authorize
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
                    .authenticationManagerResolver(authenticationManagerResolver)
            );
        return http.build();
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer(IUserService userService) {
        return context -> {
            if (OidcParameterNames.ID_TOKEN.equals(context.getTokenType().getValue())) {
                Optional<User> optional = userService.findById(Long.valueOf(context.getPrincipal().getName()));
                optional.ifPresent(user ->
                    context.getClaims()
                        .claim(StandardClaimNames.SUB, user.getId().toString())
                        .claim(StandardClaimNames.NAME, user.getUsername())
                        .claim(StandardClaimNames.NICKNAME, Objects.toString(user.getNickname(), ""))
                        .claim(StandardClaimNames.PICTURE, Objects.toString(user.getAvatar(), ""))
                        .claim(StandardClaimNames.EMAIL, Objects.toString(user.getEmail(), ""))
                        .claim(StandardClaimNames.EMAIL_VERIFIED, Boolean.TRUE.equals(user.getEmailVerified()))
                        .claim(StandardClaimNames.GENDER, Objects.toString(user.getGender(), ""))
                        .claim(StandardClaimNames.BIRTHDATE, Objects.toString(user.getBirthdate(), ""))
                        .claim(StandardClaimNames.PHONE_NUMBER, Objects.toString(user.getPhoneNumber(), ""))
                        .claim(StandardClaimNames.PHONE_NUMBER_VERIFIED, Boolean.TRUE.equals(user.getPhoneNumberVerified()))
                );
            }
        };
    }

}
