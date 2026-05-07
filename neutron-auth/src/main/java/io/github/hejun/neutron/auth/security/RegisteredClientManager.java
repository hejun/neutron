package io.github.hejun.neutron.auth.security;

import io.github.hejun.neutron.auth.entity.Client;
import io.github.hejun.neutron.auth.entity.Tenant;
import io.github.hejun.neutron.auth.security.util.AuthorizationServerContextUtil;
import io.github.hejun.neutron.auth.service.IClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

/**
 * 多租户 RegisteredClientRepository
 *
 * @author HeJun
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisteredClientManager implements RegisteredClientRepository {

    private final AuthorizationServerContextUtil contextUtil;
    private final IClientService clientService;

    @Override
    public void save(RegisteredClient registeredClient) {
        throw new UnsupportedOperationException("unsupport yet!");
    }

    @Override
    public RegisteredClient findById(String id) {
        if (!StringUtils.hasText(id)) {
            return null;
        }
        Optional<Client> optional = clientService.findById(Long.valueOf(id));
        return optional.map(this::toRegisteredClient).orElse(null);
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        Optional<Tenant> tenantOptional = contextUtil.getCurrentTenant();

        if (tenantOptional.isEmpty()) {
            return null;
        }

        Tenant tenant = tenantOptional.get();
        if (Boolean.FALSE.equals(tenant.getEnabled())) {
            throw new DisabledException("Tenant is locked");
        }

        Optional<Client> clientOptional = clientService.findByClientId(clientId, tenant.getId());
        return clientOptional.map(this::toRegisteredClient).orElse(null);
    }

    private RegisteredClient toRegisteredClient(Client client) {
        if (client == null) {
            return null;
        }
        RegisteredClient.Builder clientBuilder = RegisteredClient
            .withId(client.getId().toString())
            .clientId(client.getClientId());
        if (StringUtils.hasText(client.getClientSecret())) {
            clientBuilder.clientSecret(client.getClientSecret());
        }
        if (StringUtils.hasText(client.getClientName())) {
            clientBuilder.clientName(client.getClientName());
        }
        if (StringUtils.hasText(client.getClientAuthenticationMethods())) {
            Set<String> clientAuthenticationMethods = StringUtils
                .commaDelimitedListToSet(client.getClientAuthenticationMethods());
            for (String clientAuthenticationMethod : clientAuthenticationMethods) {
                clientBuilder.clientAuthenticationMethod(resolveClientAuthenticationMethod(clientAuthenticationMethod));
            }
        }
        if (StringUtils.hasText(client.getAuthorizationGrantTypes())) {
            Set<String> authorizationGrantTypes = StringUtils
                .commaDelimitedListToSet(client.getAuthorizationGrantTypes());
            for (String authorizationGrantType : authorizationGrantTypes) {
                clientBuilder.authorizationGrantType(resolveAuthorizationGrantType(authorizationGrantType));
            }
        }
        if (StringUtils.hasText(client.getRedirectUris())) {
            Set<String> redirectUris = StringUtils.commaDelimitedListToSet(client.getRedirectUris());
            for (String redirectUri : redirectUris) {
                clientBuilder.redirectUri(redirectUri);
            }
        }
        if (StringUtils.hasText(client.getPostLogoutRedirectUris())) {
            Set<String> postLogoutRedirectUris = StringUtils.commaDelimitedListToSet(client.getPostLogoutRedirectUris());
            for (String postLogoutRedirectUri : postLogoutRedirectUris) {
                clientBuilder.postLogoutRedirectUri(postLogoutRedirectUri);
            }
        }
        if (StringUtils.hasText(client.getScopes())) {
            Set<String> scopes = StringUtils.commaDelimitedListToSet(client.getScopes());
            for (String scope : scopes) {
                clientBuilder.scope(scope);
            }
        }

        ClientSettings.Builder clientSettingsBuilder = ClientSettings.builder();
        if (Boolean.TRUE.equals(client.getRequireProofKey())) {
            clientSettingsBuilder.requireProofKey(client.getRequireProofKey());
        }
        if (Boolean.TRUE.equals(client.getRequireAuthorizationConsent())) {
            clientSettingsBuilder.requireAuthorizationConsent(true);
        }
        clientBuilder.clientSettings(clientSettingsBuilder.build());

        TokenSettings.Builder tokenSettingsBuilder = TokenSettings.builder();
        if (client.getAccessTokenTimeToLive() != null) {
            tokenSettingsBuilder.accessTokenTimeToLive(Duration.ofSeconds(client.getAccessTokenTimeToLive()));
        }
        if (client.getRefreshTokenTimeToLive() != null) {
            tokenSettingsBuilder.refreshTokenTimeToLive(Duration.ofSeconds(client.getRefreshTokenTimeToLive()));
        }
        tokenSettingsBuilder.reuseRefreshTokens(false);
        clientBuilder.tokenSettings(tokenSettingsBuilder.build());
        return clientBuilder.build();
    }

    private static AuthorizationGrantType resolveAuthorizationGrantType(String authorizationGrantType) {
        if (AuthorizationGrantType.AUTHORIZATION_CODE.getValue().equals(authorizationGrantType)) {
            return AuthorizationGrantType.AUTHORIZATION_CODE;
        } else if (AuthorizationGrantType.CLIENT_CREDENTIALS.getValue().equals(authorizationGrantType)) {
            return AuthorizationGrantType.CLIENT_CREDENTIALS;
        } else if (AuthorizationGrantType.REFRESH_TOKEN.getValue().equals(authorizationGrantType)) {
            return AuthorizationGrantType.REFRESH_TOKEN;
        }
        return new AuthorizationGrantType(authorizationGrantType);
    }

    private static ClientAuthenticationMethod resolveClientAuthenticationMethod(String clientAuthenticationMethod) {
        if (ClientAuthenticationMethod.CLIENT_SECRET_BASIC.getValue().equals(clientAuthenticationMethod)) {
            return ClientAuthenticationMethod.CLIENT_SECRET_BASIC;
        } else if (ClientAuthenticationMethod.CLIENT_SECRET_POST.getValue().equals(clientAuthenticationMethod)) {
            return ClientAuthenticationMethod.CLIENT_SECRET_POST;
        } else if (ClientAuthenticationMethod.NONE.getValue().equals(clientAuthenticationMethod)) {
            return ClientAuthenticationMethod.NONE;
        }
        return new ClientAuthenticationMethod(clientAuthenticationMethod);
    }

}
