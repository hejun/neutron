package io.github.hejun.neutron.security.issuer;

import io.github.hejun.neutron.security.issuer.authorization.AuthorizationServerContextEnhanceFilter;
import org.springframework.context.ApplicationContext;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * 多租户Context增强配置
 *
 * @author HeJun
 */
public class AuthorizationServerContextEnhanceConfigurer
	extends AbstractHttpConfigurer<AuthorizationServerContextEnhanceConfigurer, HttpSecurity> {

	@Override
	public void configure(HttpSecurity http) throws Exception {
		AuthorizationServerSettings authorizationServerSettings = this.getAuthorizationServerSettings(http);

		OAuth2AuthorizationServerConfigurer auth2AuthorizationServerConfigurer = http
			.getConfigurer(OAuth2AuthorizationServerConfigurer.class);

		if (auth2AuthorizationServerConfigurer != null) {
			this.enhanceConfigure(http);
			AuthorizationServerContextEnhanceFilter enhanceFilter =
				new AuthorizationServerContextEnhanceFilter(authorizationServerSettings);
			http.addFilterAfter(postProcess(enhanceFilter), SecurityContextHolderFilter.class);
		} else {
			RequestCache requestCache = http.getSharedObject(RequestCache.class);
			OAuth2AuthorizationService oAuth2AuthorizationService = getOAuth2AuthorizationService(http);
			AuthorizationServerContextEnhanceFilter enhanceFilter =
				new AuthorizationServerContextEnhanceFilter(authorizationServerSettings, requestCache, oAuth2AuthorizationService);
			http.addFilterAfter(postProcess(enhanceFilter), SecurityContextHolderFilter.class);
		}
	}

	private OAuth2AuthorizationService getOAuth2AuthorizationService(HttpSecurity http) {
		OAuth2AuthorizationService oAuth2AuthorizationService = http.getSharedObject(OAuth2AuthorizationService.class);
		if (oAuth2AuthorizationService == null) {
			ApplicationContext context = http.getSharedObject(ApplicationContext.class);
			oAuth2AuthorizationService = context.getBean(OAuth2AuthorizationService.class);
		}
		return oAuth2AuthorizationService;
	}

	private AuthorizationServerSettings getAuthorizationServerSettings(HttpSecurity http) {
		AuthorizationServerSettings authorizationServerSettings = http.getSharedObject(AuthorizationServerSettings.class);
		if (authorizationServerSettings == null) {
			ApplicationContext context = http.getSharedObject(ApplicationContext.class);
			authorizationServerSettings = context.getBean(AuthorizationServerSettings.class);
		}
		return authorizationServerSettings;
	}

	/**
	 * 通过反射移除 AuthorizationServerContextFilter 并添加自定义的 AuthorizationServerContextEnhanceFilter
	 *
	 * @param http HttpSecurity
	 * @throws Exception 反射异常
	 */
	private void enhanceConfigure(HttpSecurity http) throws Exception {
		final String targetClassName =
			"org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.AuthorizationServerContextFilter";

		// 移除注册的排序
		Field filterOrdersField = ReflectionUtils.findField(HttpSecurity.class, "filterOrders");
		if (filterOrdersField != null) {
			ReflectionUtils.makeAccessible(filterOrdersField);
			Object filterOrders = ReflectionUtils.getField(filterOrdersField, http);

			Class<?> filterOrderRegistrationClass = Class
				.forName("org.springframework.security.config.annotation.web.builders.FilterOrderRegistration");
			Field filterToOrderField = ReflectionUtils.findField(filterOrderRegistrationClass, "filterToOrder");
			if (filterToOrderField != null) {
				ReflectionUtils.makeAccessible(filterToOrderField);
				if (ReflectionUtils.getField(filterToOrderField, filterOrders) instanceof Map<?, ?> filterToOrder) {
					filterToOrder.remove(targetClassName);
				}
			}
		}

		// 移除 Filter
		Field filtersField = ReflectionUtils.findField(HttpSecurity.class, "filters");
		if (filtersField != null) {
			ReflectionUtils.makeAccessible(filtersField);
			Class<?> orderedFilterClass = Class
				.forName("org.springframework.security.config.annotation.web.builders.HttpSecurity$OrderedFilter");
			Field filterField = ReflectionUtils.findField(orderedFilterClass, "filter");
			if (filterField != null) {
				ReflectionUtils.makeAccessible(filterField);
				if (ReflectionUtils.getField(filtersField, http) instanceof List<?> filters) {
					Object targetFilter = null;
					for (Object filter : filters) {
						Object orderedFilter = ReflectionUtils.getField(filterField, filter);
						if (orderedFilter != null) {
							Class<?> filterClass = orderedFilter.getClass();
							if (filterClass.getName().equals(targetClassName)) {
								targetFilter = filter;
								break;
							}
						}
					}
					if (targetFilter != null) {
						filters.remove(targetFilter);
					}
				}
			}
		}
	}

}
