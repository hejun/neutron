package io.github.hejun.neutron.config;

import io.github.hejun.neutron.entity.Client;
import io.github.hejun.neutron.entity.Tenant;
import io.github.hejun.neutron.entity.User;
import io.github.hejun.neutron.properties.init.InitializeClientProperties;
import io.github.hejun.neutron.properties.init.InitializeTenantProperties;
import io.github.hejun.neutron.properties.init.InitializeUserProperties;
import io.github.hejun.neutron.service.IClientService;
import io.github.hejun.neutron.service.ITenantService;
import io.github.hejun.neutron.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;

/**
 * 初始化配置
 *
 * @author HeJun
 */
@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@ConfigurationPropertiesScan(basePackages = {"io.github.hejun.neutron.properties"})
public class InitializeConfig implements ApplicationListener<ApplicationStartedEvent> {

	private final InitializeTenantProperties initializeTenantProperties;
	private final InitializeClientProperties initializeClientProperties;
	private final InitializeUserProperties initializeUserProperties;
	private final ITenantService tenantService;
	private final IClientService clientService;
	private final IUserService userService;

	@Override
	public void onApplicationEvent(ApplicationStartedEvent event) {
		String tenantCode = initializeTenantProperties.getCode();
		if (StringUtils.isNotBlank(tenantCode)) {
			Tenant exist = tenantService.findByCode(tenantCode);
			// 如果不存在, 说明没有初始化过, 需要初始化
			if (exist == null) {
				Tenant tenant = new Tenant();
				tenant.setCode(initializeTenantProperties.getCode());
				tenant.setName(initializeTenantProperties.getName());
				tenantService.save(tenant);

				Client client = new Client();
				client.setClientId(initializeClientProperties.getClientId());
				client.setName(initializeTenantProperties.getName());
				client.setAuthenticationMethods(ClientAuthenticationMethod.NONE.getValue());
				client.setAuthorizationGrantTypes(AuthorizationGrantType.AUTHORIZATION_CODE.getValue());
				client.setRedirectUris(initializeClientProperties.getRedirectUri());
				client.setScopes(String.join(",", OidcScopes.OPENID, OidcScopes.PROFILE));
				client.setRequireProofKey(true);
				client.setRequireAuthorizationConsent(true);
				client.setAccessTokenTimeToLive(500);
				client.setRefreshTokenTimeToLive(1500);
				client.setEnabled(true);
				client.setTenant(tenant);
				clientService.save(client);

				User user = new User();
				user.setUsername(initializeUserProperties.getUsername());
				user.setPassword(initializeUserProperties.getPassword());
				user.setEnabled(true);
				user.setTenant(tenant);
				userService.save(user);
			}
		}
	}

}
