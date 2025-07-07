package io.github.hejun.neutron.security.issuer;

import io.github.hejun.neutron.security.issuer.context.AuthorizationServerContextEnhanceFilter;
import jakarta.servlet.Filter;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 多租户Context增强配置
 *
 * @author HeJun
 */
public class AuthorizationServerContextEnhanceConfigurer
	extends AbstractHttpConfigurer<AuthorizationServerContextEnhanceConfigurer, HttpSecurity> {

	private final List<RequestMatcher> requestMatchers = new ArrayList<>();

	@Override
	public void configure(HttpSecurity http) throws Exception {
		AuthorizationServerSettings authorizationServerSettings = this.getAuthorizationServerSettings(http);

		OAuth2AuthorizationServerConfigurer auth2AuthorizationServerConfigurer = http.getConfigurer(OAuth2AuthorizationServerConfigurer.class);
		if (auth2AuthorizationServerConfigurer != null) {
			this.enhanceConfigure(http);
			AuthorizationServerContextEnhanceFilter enhanceFilter =
				new AuthorizationServerContextEnhanceFilter(authorizationServerSettings);
			http.addFilterAfter(postProcess(enhanceFilter), SecurityContextHolderFilter.class);
		} else {
			RequestCache requestCache = http.getSharedObject(RequestCache.class);
			AuthorizationServerContextEnhanceFilter enhanceFilter =
				new AuthorizationServerContextEnhanceFilter(authorizationServerSettings, requestCache, new OrRequestMatcher(requestMatchers));
			http.addFilterAfter(postProcess(enhanceFilter), SecurityContextHolderFilter.class);
		}
	}

	public AuthorizationServerContextEnhanceConfigurer requestMatcher(String path) {
		requestMatchers.add(PathPatternRequestMatcher.withDefaults().matcher(path));
		return this;
	}

	public AuthorizationServerContextEnhanceConfigurer requestMatcher(HttpMethod method, String path) {
		requestMatchers.add(PathPatternRequestMatcher.withDefaults().matcher(method, path));
		return this;
	}

	private AuthorizationServerSettings getAuthorizationServerSettings(HttpSecurity http) {
		AuthorizationServerSettings authorizationServerSettings = http.getSharedObject(AuthorizationServerSettings.class);
		if (authorizationServerSettings == null) {
			ApplicationContext context = http.getSharedObject(ApplicationContext.class);
			authorizationServerSettings = context.getBean(AuthorizationServerSettings.class);
		}
		return authorizationServerSettings;
	}

	private void enhanceConfigure(HttpSecurity http) throws Exception {
		// 通过反射移除 AuthorizationServerContextFilter
		@SuppressWarnings("unchecked")
		Class<? extends OncePerRequestFilter> authorizationServerContextFilterClass = (Class<? extends OncePerRequestFilter>) Class
			.forName("org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.AuthorizationServerContextFilter");

		// 移除排序
		Field filterOrdersField = ReflectionUtils.findField(HttpSecurity.class, "filterOrders");
		if (filterOrdersField != null) {
			ReflectionUtils.makeAccessible(filterOrdersField);
			Object filterOrders = ReflectionUtils.getField(filterOrdersField, http);

			Class<?> filterOrderRegistrationClass = Class.forName("org.springframework.security.config.annotation.web.builders.FilterOrderRegistration");
			Field filterToOrderField = ReflectionUtils.findField(filterOrderRegistrationClass, "filterToOrder");
			if (filterToOrderField != null) {
				ReflectionUtils.makeAccessible(filterToOrderField);
				@SuppressWarnings("unchecked")
				Map<String, Integer> filterToOrder = (Map<String, Integer>) ReflectionUtils.getField(filterToOrderField, filterOrders);
				if (filterToOrder != null) {
					filterToOrder.remove(authorizationServerContextFilterClass.getName());
				}
			}
		}

		// 移除 Filter
		Field filtersField = ReflectionUtils.findField(HttpSecurity.class, "filters");
		if (filtersField != null) {
			ReflectionUtils.makeAccessible(filtersField);
			Class<?> orderedFilterClass = Class.forName("org.springframework.security.config.annotation.web.builders.HttpSecurity$OrderedFilter");
			Field filterField = ReflectionUtils.findField(orderedFilterClass, "filter");
			if (filterField != null) {
				ReflectionUtils.makeAccessible(filterField);
				@SuppressWarnings("unchecked")
				List<? extends Filter> filters = (List<? extends Filter>) ReflectionUtils.getField(filtersField, http);
				if (filters != null) {
					Filter targetFilter = null;
					for (Filter filter : filters) {
						Object orderedFilter = ReflectionUtils.getField(filterField, filter);
						if (orderedFilter != null) {
							Class<?> filterClass = orderedFilter.getClass();
							if (filterClass.getName().equals(authorizationServerContextFilterClass.getName())) {
								targetFilter = filter;
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
