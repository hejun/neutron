package io.github.hejun.neutron.security.component;

import io.github.hejun.neutron.properties.InitializeProperties;
import io.github.hejun.neutron.util.ContextUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class RegisteredClientRepositoryImpl implements RegisteredClientRepository {

	private final InitializeProperties initializeProperties;

	private RegisteredClientRepository registeredClientRepository;

	@PostConstruct
	public void init() {
		RegisteredClient client = RegisteredClient
			.withId(initializeProperties.client().id())
			.clientId(initializeProperties.client().id())
			.clientName(initializeProperties.client().name())
			.clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
			.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
			.redirectUris(redirectUris -> redirectUris.addAll(initializeProperties.client().redirectUris()))
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
		registeredClientRepository = new InMemoryRegisteredClientRepository(client);
	}

	@Override
	public void save(RegisteredClient registeredClient) {
		String issuer = ContextUtil.getIssuer();
		if (log.isDebugEnabled()) {
			log.debug("save, current issuer: {}", issuer);
		}
		registeredClientRepository.save(registeredClient);
	}

	@Override
	public RegisteredClient findById(String id) {
		String issuer = ContextUtil.getIssuer();
		if (log.isDebugEnabled()) {
			log.debug("findById, current issuer: {}", issuer);
		}
		return registeredClientRepository.findById(id);
	}

	@Override
	public RegisteredClient findByClientId(String clientId) {
		String issuer = ContextUtil.getIssuer();
		if (log.isDebugEnabled()) {
			log.debug("findByClientId, current issuer: {}", issuer);
		}
		return registeredClientRepository.findByClientId(clientId);
	}

}
