package io.github.hejun.neutron.config;

import io.github.hejun.neutron.properties.InitializeProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.time.Duration;

/**
 * 扩展配置
 *
 * @author HeJun
 */
@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ExtraConfig {

	@Bean
	public RegisteredClientRepository registeredClientRepository(InitializeProperties properties) {
		RegisteredClient client = RegisteredClient
			.withId(properties.client().id())
			.clientId(properties.client().id())
			.clientName(properties.client().name())
			.clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
			.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
			.redirectUris(redirectUris -> redirectUris.addAll(properties.client().redirectUris()))
			.scope(OidcScopes.OPENID)
			.scope(OidcScopes.PROFILE)
			.clientSettings(ClientSettings.builder()
				.requireProofKey(true)
				.requireAuthorizationConsent(true)
				.build())
			.tokenSettings(TokenSettings.builder()
				.accessTokenTimeToLive(Duration.ofMinutes(5))
				.build())
			.build();
		return new InMemoryRegisteredClientRepository(client);
	}

	@Bean
	public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
		return username -> User.builder()
			.username(username)
			.password("1234")
			.roles("USER")
			.passwordEncoder(passwordEncoder::encode)
			.build();
	}

}
