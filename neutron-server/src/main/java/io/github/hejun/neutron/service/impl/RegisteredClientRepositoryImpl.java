package io.github.hejun.neutron.service.impl;

import io.github.hejun.neutron.util.ContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 多租户 RegisteredClientRepository
 *
 * @author HeJun
 */
@Slf4j
@Component
public class RegisteredClientRepositoryImpl implements RegisteredClientRepository {

	private final RegisteredClientRepository delegate;

	public RegisteredClientRepositoryImpl() {
		RegisteredClient client = RegisteredClient
			.withId("neutron")
			.clientId("neutron")
			.clientName("Neutron")
			.clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
			.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
			.redirectUri("http://localhost:5173/callback")
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
		delegate = new InMemoryRegisteredClientRepository(client);
	}

	@Override
	public void save(RegisteredClient registeredClient) {
		String issuer = ContextUtils.getIssuer();
		if (log.isDebugEnabled()) {
			log.debug("save, current issuer: {}", issuer);
		}
		delegate.save(registeredClient);
	}

	@Override
	public RegisteredClient findById(String id) {
		String issuer = ContextUtils.getIssuer();
		if (log.isDebugEnabled()) {
			log.debug("findById, current issuer: {}", issuer);
		}
		return delegate.findById(id);
	}

	@Override
	public RegisteredClient findByClientId(String clientId) {
		String issuer = ContextUtils.getIssuer();
		if (log.isDebugEnabled()) {
			log.debug("findByClientId, current issuer: {}", issuer);
		}
		return delegate.findByClientId(clientId);
	}

}
