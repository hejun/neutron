package io.github.hejun.neutron.config;

import io.github.hejun.neutron.entity.Client;
import io.github.hejun.neutron.entity.Tenant;
import io.github.hejun.neutron.entity.User;
import io.github.hejun.neutron.properties.InitializeProperties;
import io.github.hejun.neutron.service.IClientService;
import io.github.hejun.neutron.service.ITenantService;
import io.github.hejun.neutron.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;

/**
 * 初始化 默认信息
 *
 * @author HeJun
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class InitializeConfig implements SmartInitializingSingleton {

	private final InitializeProperties initializeProperties;
	private final ITenantService tenantService;
	private final IClientService clientService;
	private final IUserService userService;
	private final PasswordEncoder passwordEncoder;

	@Override
	public void afterSingletonsInstantiated() {
		if (initializeProperties != null && StringUtils.hasText(initializeProperties.tenant().issuer())) {
			Tenant tenant = tenantService.findByIssuer(initializeProperties.tenant().issuer());
			if (tenant == null) {
				log.info("Initializing begging");

				tenant = this.initializeTenant();
				Client client = this.initializeClient(tenant);
				this.initializeUser(tenant, client);

				log.info("Initializing end");
			}
		}
	}

	private Tenant initializeTenant() {
		Tenant tenant = new Tenant();
		tenant.setName(initializeProperties.tenant().name());
		tenant.setIssuer(initializeProperties.tenant().issuer());
		return tenantService.save(tenant);
	}

	private Client initializeClient(Tenant tenant) {
		Client client = new Client();
		client.setClientId(initializeProperties.client().clientId());
		client.setName(initializeProperties.client().name());
		client.setClientAuthenticationMethods(ClientAuthenticationMethod.NONE.getValue());
		client.setAuthorizationGrantTypes(AuthorizationGrantType.AUTHORIZATION_CODE.getValue());
		client.setRedirectUris(StringUtils.collectionToCommaDelimitedString(initializeProperties.client().redirectUris()));
		client.setScopes(StringUtils.collectionToCommaDelimitedString(Arrays.asList(OidcScopes.OPENID, OidcScopes.PROFILE)));
		client.setRequireProofKey(true);
		client.setRequireAuthorizationConsent(true);
		client.setTenantId(tenant.getId());
		return clientService.save(client);
	}

	private void initializeUser(Tenant tenant, Client client) {
		User user = new User();
		user.setUsername(initializeProperties.user().name());
		user.setPassword(passwordEncoder.encode(initializeProperties.user().password()));
		user.setTenantId(tenant.getId());
		userService.save(user);
		userService.saveUserClient(user.getId(), client.getId());
	}

}
