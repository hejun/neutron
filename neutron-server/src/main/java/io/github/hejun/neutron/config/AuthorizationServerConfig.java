package io.github.hejun.neutron.config;

import com.nimbusds.jwt.JWTParser;
import io.github.hejun.neutron.entity.Tenant;
import io.github.hejun.neutron.security.issuer.AuthorizationServerContextEnhanceConfigurer;
import io.github.hejun.neutron.service.ITenantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcLogoutAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcUserInfoAuthenticationContext;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcUserInfoAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.oidc.web.authentication.OidcLogoutAuthenticationConverter;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.util.Map;
import java.util.function.Function;

/**
 * 授权服务器配置
 *
 * @author HeJun
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
public class AuthorizationServerConfig {

	private final Function<OidcUserInfoAuthenticationContext, OidcUserInfo> userInfoMapper = (context) -> {
		OidcUserInfoAuthenticationToken authentication = context.getAuthentication();
		JwtAuthenticationToken principal = (JwtAuthenticationToken) authentication.getPrincipal();

		Object audName = "";
		OAuth2Authorization.Token<OidcIdToken> oidcIdToken = context.getAuthorization().getToken(OidcIdToken.class);
		if (oidcIdToken != null && oidcIdToken.getToken().getClaims() != null) {
			audName = oidcIdToken.getToken().getClaims().get("aud_name");
		}

		return new OidcUserInfo(Map.of(
			"iss", principal.getToken().getIssuer(),
			"iss_name", principal.getToken().getClaims().get("iss_name"),
			"aud", principal.getToken().getAudience().getFirst(),
			"aud_name", audName,
			"sub", principal.getName()
		));
	};

	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE)
	public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http,
																	  RegisteredClientRepository registeredClientRepository) throws Exception {
		OAuth2AuthorizationServerConfigurer configurer = OAuth2AuthorizationServerConfigurer.authorizationServer();
		http
			.securityMatcher(configurer.getEndpointsMatcher())
			.with(configurer, authorizationServer ->
				authorizationServer
					.authorizationEndpoint(authorizationEndpoint ->
						authorizationEndpoint.consentPage("/consent")
					)
					.oidc(oidc -> oidc
						.userInfoEndpoint(userinfo -> userinfo.userInfoMapper(userInfoMapper))
						.logoutEndpoint(logout -> logout.logoutRequestConverter(createOidcLogoutAuthenticationConverter(registeredClientRepository)))
					)
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

	// 当未传递登出跳转地址且Client有配置登出地址时, 默认跳配置的第一个地址
	private AuthenticationConverter createOidcLogoutAuthenticationConverter(RegisteredClientRepository registeredClientRepository) {
		OidcLogoutAuthenticationConverter converter = new OidcLogoutAuthenticationConverter();
		return request -> {
			OidcLogoutAuthenticationToken authentication = (OidcLogoutAuthenticationToken) converter.convert(request);

			if (!StringUtils.hasText(authentication.getPostLogoutRedirectUri())) {
				String clientId = null;
				try {
					clientId = JWTParser.parse(authentication.getIdTokenHint()).getJWTClaimsSet().getAudience().getFirst();
				} catch (ParseException e) {
					log.error("Parser IdTokenHint error: {}", e.getMessage());
				}
				if (clientId != null) {
					RegisteredClient registeredClient = registeredClientRepository.findByClientId(clientId);
					if (registeredClient != null && !CollectionUtils.isEmpty(registeredClient.getPostLogoutRedirectUris())) {
						String postLogoutRedirectUri = registeredClient.getPostLogoutRedirectUris().stream().findFirst().orElse(null);
						if (postLogoutRedirectUri != null) {
							authentication = new OidcLogoutAuthenticationToken(authentication.getIdTokenHint(), (Authentication) authentication.getPrincipal(), authentication.getSessionId(), authentication.getClientId(), postLogoutRedirectUri, authentication.getState());
						}
					}
				}
			}

			return authentication;
		};
	}


	@Bean
	public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer(ITenantService tenantService) {
		return (context) -> {
			if (OidcParameterNames.ID_TOKEN.equals(context.getTokenType().getValue())) {
				Tenant tenant = tenantService.findByIssuer(context.getAuthorizationServerContext().getIssuer());
				if (tenant != null) {
					context.getClaims().claim("iss_name", tenant.getName());
				}
				context.getClaims().claim("aud_name", context.getRegisteredClient().getClientName());
			}
			if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
				Tenant tenant = tenantService.findByIssuer(context.getAuthorizationServerContext().getIssuer());
				if (tenant != null) {
					context.getClaims().claim("iss_name", tenant.getName());
				}
				context.getClaims().claim("aud_name", context.getRegisteredClient().getClientName());
			}
		};
	}

}
