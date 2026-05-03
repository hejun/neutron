package io.github.hejun.auth.security;

import io.github.hejun.auth.security.issuer.AuthorizationServerMultiIssuerContextFilter;
import org.springframework.context.ApplicationContext;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.savedrequest.RequestCache;

/**
 * 多租户配置
 *
 * @author HeJun
 */
public class AuthorizationServerMultiIssuerConfigurer
	extends AbstractHttpConfigurer<AuthorizationServerMultiIssuerConfigurer, HttpSecurity> {

	@Override
	public void configure(HttpSecurity http) {
		AuthorizationServerSettings authorizationServerSettings = this.getSharedObject(http, AuthorizationServerSettings.class);

		if (authorizationServerSettings.isMultipleIssuersAllowed()) {
			RequestCache requestCache = this.getSharedObject(http, RequestCache.class);
			OAuth2AuthorizationService authorizationService = this.getSharedObject(http, OAuth2AuthorizationService.class);

			AuthorizationServerMultiIssuerContextFilter filter
				= new AuthorizationServerMultiIssuerContextFilter(requestCache, authorizationService, authorizationServerSettings);

			http.addFilterAfter(postProcess(filter), SecurityContextHolderFilter.class);
		}

		super.configure(http);
	}

	private <C> C getSharedObject(HttpSecurity http, Class<C> sharedType) {
		C sharedObject = http.getSharedObject(sharedType);
		if (sharedObject == null) {
			ApplicationContext context = http.getSharedObject(ApplicationContext.class);
			sharedObject = context.getBean(sharedType);
		}
		return sharedObject;
	}

}
