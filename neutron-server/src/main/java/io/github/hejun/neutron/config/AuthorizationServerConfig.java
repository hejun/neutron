package io.github.hejun.neutron.config;

import io.github.hejun.neutron.security.issuer.AuthorizationServerContextEnhanceConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcUserInfoAuthenticationContext;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcUserInfoAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

import java.util.Map;
import java.util.function.Function;

/**
 * 授权服务器配置
 *
 * @author HeJun
 */
@Configuration(proxyBeanMethods = false)
public class AuthorizationServerConfig {

	private final Function<OidcUserInfoAuthenticationContext, OidcUserInfo> userInfoMapper = (context) -> {
		OidcUserInfoAuthenticationToken authentication = context.getAuthentication();
		JwtAuthenticationToken principal = (JwtAuthenticationToken) authentication.getPrincipal();

		Object audName = "";
		OAuth2Authorization.Token<OidcIdToken> oidcIdToken = context.getAuthorization().getToken(OidcIdToken.class);
		if (oidcIdToken != null && oidcIdToken.getClaims() != null) {
			audName = oidcIdToken.getClaims().get("aud_name");
		}

		return new OidcUserInfo(Map.of(
			"iss", principal.getToken().getIssuer(),
			"aud", principal.getToken().getAudience().getFirst(),
			"aud_name", audName,
			"sub", principal.getName()
		));
	};

	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE)
	public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
		OAuth2AuthorizationServerConfigurer configurer = OAuth2AuthorizationServerConfigurer.authorizationServer();
		http
			.securityMatcher(configurer.getEndpointsMatcher())
			.with(configurer, authorizationServer ->
				authorizationServer
					.authorizationEndpoint(authorizationEndpoint ->
						authorizationEndpoint.consentPage("/consent")
					)
					.oidc(oidc -> oidc.userInfoEndpoint(userinfo -> userinfo.userInfoMapper(userInfoMapper)))
			)
			.authorizeHttpRequests(authorizeRequests ->
				authorizeRequests.anyRequest().authenticated()
			)
			.cors(Customizer.withDefaults())
			.exceptionHandling(exceptionHandling ->
				exceptionHandling
					.defaultAuthenticationEntryPointFor(
						new LoginUrlAuthenticationEntryPoint("/login"),
						new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
					)
			);
		// 自有 AuthorizationServerContext 缺少信息, 禁用原有的,使用自定义增强添加
		http.with(new AuthorizationServerContextEnhanceConfigurer(), Customizer.withDefaults());
		return http.build();
	}

	@Bean
	public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
		return (context) -> {
			if (OidcParameterNames.ID_TOKEN.equals(context.getTokenType().getValue())) {
				context.getClaims().claim("aud_name", context.getRegisteredClient().getClientName());
			}
		};
	}

}
