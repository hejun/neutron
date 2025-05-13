package io.github.hejun.neutron.conveter;

import io.github.hejun.neutron.dto.ClientDTO;
import io.github.hejun.neutron.entity.Client;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.time.Duration;

/**
 * Client Entity与DTO转换
 *
 * @author HeJun
 */
@Mapper(componentModel = "spring")
public interface ClientConverter {

	ClientDTO convert(Client client);

	Client convert(ClientDTO clientDTO);

	default RegisteredClient toRegisteredClient(Client client) {
		if (client == null) {
			return null;
		}

		RegisteredClient.Builder clientBuilder = RegisteredClient
			.withId(client.getId())
			.clientId(client.getClientId())
			.clientName(client.getName());

		if (StringUtils.isNotBlank(client.getAuthenticationMethods())) {
			String[] methods = client.getAuthenticationMethods().split(",");
			for (String method : methods) {
				clientBuilder.clientAuthenticationMethod(new ClientAuthenticationMethod(method));
			}
		}

		if (StringUtils.isNotBlank(client.getAuthorizationGrantTypes())) {
			String[] grantTypes = client.getAuthorizationGrantTypes().split(",");
			for (String grantType : grantTypes) {
				clientBuilder.authorizationGrantType(new AuthorizationGrantType(grantType));
			}
		}

		if (StringUtils.isNotBlank(client.getRedirectUris())) {
			String[] redirectUris = client.getRedirectUris().split(",");
			for (String redirectUri : redirectUris) {
				clientBuilder.redirectUri(redirectUri);
			}
		}

		if (StringUtils.isNotBlank(client.getScopes())) {
			String[] scopes = client.getScopes().split(",");
			for (String scope : scopes) {
				clientBuilder.scope(scope);
			}
		}

		ClientSettings.Builder clientSettingsBuilder = ClientSettings.builder();
		if (client.getRequireProofKey() != null) {
			clientSettingsBuilder.requireProofKey(client.getRequireProofKey());
		}
		if (client.getRequireAuthorizationConsent() != null) {
			clientSettingsBuilder.requireAuthorizationConsent(client.getRequireAuthorizationConsent());
		}
		clientBuilder.clientSettings(clientSettingsBuilder.build());

		TokenSettings.Builder tokenSettingsBuilder = TokenSettings.builder();
		if (client.getAccessTokenTimeToLive() != null) {
			tokenSettingsBuilder.accessTokenTimeToLive(Duration.ofSeconds(client.getAccessTokenTimeToLive()));
		}
		if (client.getRefreshTokenTimeToLive() != null) {
			tokenSettingsBuilder.refreshTokenTimeToLive(Duration.ofSeconds(client.getRefreshTokenTimeToLive()));
		}
		clientBuilder.tokenSettings(tokenSettingsBuilder.build());

		return clientBuilder.build();
	}

}
