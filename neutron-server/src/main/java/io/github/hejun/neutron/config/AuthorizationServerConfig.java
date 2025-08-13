package io.github.hejun.neutron.config;

import com.nimbusds.jwt.JWTParser;
import io.github.hejun.neutron.entity.Client;
import io.github.hejun.neutron.entity.Tenant;
import io.github.hejun.neutron.security.issuer.AuthorizationServerContextEnhanceConfigurer;
import io.github.hejun.neutron.service.IClientService;
import io.github.hejun.neutron.service.ITenantService;
import io.github.hejun.neutron.util.ContextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcLogoutAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.oidc.web.authentication.OidcLogoutAuthenticationConverter;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.text.ParseException;

/**
 * 授权服务器配置
 *
 * @author HeJun
 */
@Configuration(proxyBeanMethods = false)
public class AuthorizationServerConfig {

	private static final Logger log = LoggerFactory.getLogger(AuthorizationServerConfig.class);

	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE)
	public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http, RegisteredClientRepository registeredClientRepository) throws Exception {
		OAuth2AuthorizationServerConfigurer configurer = OAuth2AuthorizationServerConfigurer.authorizationServer();
		http.securityMatcher(configurer.getEndpointsMatcher()).with(configurer, authorizationServer -> authorizationServer.authorizationEndpoint(authorizationEndpoint -> authorizationEndpoint.consentPage("/consent")).oidc(oidc -> oidc.logoutEndpoint(logoutEndpoint -> logoutEndpoint.logoutRequestConverter(createOidcLogoutAuthenticationConverter(registeredClientRepository))))).authorizeHttpRequests(authorizeRequests -> authorizeRequests.anyRequest().authenticated()).cors(Customizer.withDefaults()).exceptionHandling(exceptionHandling -> exceptionHandling.defaultAuthenticationEntryPointFor(new LoginUrlAuthenticationEntryPoint("/login"), new MediaTypeRequestMatcher(MediaType.TEXT_HTML)));
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
	public OAuth2TokenCustomizer<JwtEncodingContext> jwtOAuth2TokenCustomizer(ITenantService tenantService,
																		   IClientService clientService) {
		return context -> {
			if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
				Tenant tenant = tenantService.findByIssuer(ContextUtils.getIssuer());
				if (tenant != null) {
					context.getClaims().claim("iss_id", tenant.getId());
					Client client = clientService.findByClientId(tenant.getId(), ContextUtils.getClientId());
					if (client != null) {
						context.getClaims().claim("aud_id", client.getId());
					}
				}
			}
		};
	}

}
