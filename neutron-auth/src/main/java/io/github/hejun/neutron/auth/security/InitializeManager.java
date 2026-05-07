package io.github.hejun.neutron.auth.security;

import io.github.hejun.neutron.auth.entity.Client;
import io.github.hejun.neutron.auth.entity.Tenant;
import io.github.hejun.neutron.auth.entity.User;
import io.github.hejun.neutron.auth.properties.InitProperties;
import io.github.hejun.neutron.auth.repository.TenantRepository;
import io.github.hejun.neutron.auth.service.IClientService;
import io.github.hejun.neutron.auth.service.ITenantService;
import io.github.hejun.neutron.auth.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 初始化
 *
 * @author HeJun
 */
@Slf4j
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(InitProperties.class)
public class InitializeManager implements ApplicationRunner {

    private final InitProperties initProperties;
    private final TenantRepository tenantRepository;
    private final ITenantService tenantService;
    private final IClientService clientService;
    private final IUserService userService;


    @Override
    @Transactional
    public void run(@Nullable ApplicationArguments args) {
        if (tenantRepository.count() <= 0) {
            log.info("The system is being used for the first time, starting initialization");
            Assert.notNull(initProperties.tenant(), "Tenant properties is required");
            Assert.notNull(initProperties.tenant().name(), "Tenant name properties is required");
            Assert.notNull(initProperties.tenant().issuer(), "Tenant issuer properties is required");

            Assert.notNull(initProperties.client(), "Client properties is required");

            Tenant tenant = new Tenant();
            tenant.setName(initProperties.tenant().name());
            tenant.setIssuer(initProperties.tenant().issuer());
            tenantService.save(tenant);

            Client client = new Client();
            client.setClientId(initProperties.client().clientId());

            List<String> clientAuthenticationMethods = new ArrayList<>();
            List<String> authorizationGrantTypes = new ArrayList<>();

            authorizationGrantTypes.add(AuthorizationGrantType.AUTHORIZATION_CODE.getValue());

            if (StringUtils.hasText(initProperties.client().clientSecret())) {
                client.setClientSecret(initProperties.client().clientSecret());

                clientAuthenticationMethods.add(ClientAuthenticationMethod.CLIENT_SECRET_BASIC.getValue());
                authorizationGrantTypes.add(AuthorizationGrantType.REFRESH_TOKEN.getValue());
            } else {
                clientAuthenticationMethods.add(ClientAuthenticationMethod.NONE.getValue());
            }

            client.setClientName(initProperties.client().clientName());
            client.setClientAuthenticationMethods(StringUtils.collectionToCommaDelimitedString(clientAuthenticationMethods));
            client.setAuthorizationGrantTypes(StringUtils.collectionToCommaDelimitedString(authorizationGrantTypes));
            client.setRedirectUris(StringUtils.collectionToCommaDelimitedString(initProperties.client().redirectUris()));
            client.setPostLogoutRedirectUris(StringUtils.collectionToCommaDelimitedString(initProperties.client().postLogoutRedirectUris()));

            List<String> scopes = List.of(OidcScopes.OPENID, OidcScopes.PROFILE, OidcScopes.EMAIL, OidcScopes.ADDRESS, OidcScopes.PHONE);
            client.setScopes(StringUtils.collectionToCommaDelimitedString(scopes));
            client.setRequireProofKey(true);
            client.setRequireAuthorizationConsent(true);
            client.setTenant(tenant);
            clientService.save(client);

            User user = new User();
            user.setUsername(initProperties.user().username());
            user.setPassword(initProperties.user().password());
            user.setNickname(initProperties.user().nickname());
            user.setGender((byte) 1);
            user.setTenant(tenant);
            userService.save(user);
        }
    }

}
