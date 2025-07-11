package io.github.hejun.neutron.security.phone;

import io.github.hejun.neutron.security.phone.authentication.PhoneLoginAuthenticationProvider;
import io.github.hejun.neutron.security.phone.authentication.PhoneLoginEndpointFilter;
import io.github.hejun.neutron.service.UserDetailsEnhanceService;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 手机验证码登录 Configurer
 *
 * @author HeJun
 */
public class PhoneLoginConfigurer<H extends HttpSecurityBuilder<H>>
	extends AbstractAuthenticationFilterConfigurer<H, PhoneLoginConfigurer<H>, PhoneLoginEndpointFilter> {

	private static final String defaultLoginProcessingUrl = "/login/phone";

	public PhoneLoginConfigurer() {
		super(new PhoneLoginEndpointFilter(defaultLoginProcessingUrl), defaultLoginProcessingUrl);
	}

	@Override
	protected RequestMatcher createLoginProcessingUrlMatcher(String loginProcessingUrl) {
		return PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, loginProcessingUrl);
	}

	@Override
	public void init(H http) throws Exception {
		super.init(http);
		this.registerPhoneLoginCheckVerifyCodeEndpointFilter(http);
	}

	@Override
	public void configure(H http) throws Exception {
		super.configure(http);

		UserDetailsEnhanceService userDetailsEnhanceService = this.getBean(http, UserDetailsEnhanceService.class);
		OAuth2AuthorizationService authorizationService = this.getBean(http, OAuth2AuthorizationService.class);
		PhoneLoginAuthenticationProvider phoneLoginAuthenticationProvider =
			new PhoneLoginAuthenticationProvider(userDetailsEnhanceService, authorizationService);
		http.authenticationProvider(phoneLoginAuthenticationProvider);
	}

	private void registerPhoneLoginCheckVerifyCodeEndpointFilter(H http) {
		Field filterOrdersField = ReflectionUtils.findField(http.getClass(), "filterOrders");
		if (filterOrdersField != null) {
			ReflectionUtils.makeAccessible(filterOrdersField);
			Object filterOrders = ReflectionUtils.getField(filterOrdersField, http);
			if (filterOrders != null) {
				Method getOrderMethod = ReflectionUtils.findMethod(filterOrders.getClass(), "getOrder", Class.class);
				if (getOrderMethod != null) {
					ReflectionUtils.makeAccessible(getOrderMethod);
					Object order = ReflectionUtils.invokeMethod(getOrderMethod, filterOrders, UsernamePasswordAuthenticationFilter.class);
					if (order instanceof Integer intOrder) {
						Method putMethod = ReflectionUtils.findMethod(filterOrders.getClass(), "put", Class.class, int.class);
						if (putMethod != null) {
							ReflectionUtils.makeAccessible(putMethod);
							ReflectionUtils.invokeMethod(putMethod, filterOrders, getAuthenticationFilter().getClass(), intOrder + 1);
						}
					}
				}
			}
		}
	}

	private <T> T getBean(H http, Class<T> targetClass) {
		T bean = http.getSharedObject(targetClass);
		if (bean == null) {
			ApplicationContext context = http.getSharedObject(ApplicationContext.class);
			bean = context.getBean(targetClass);
		}
		return bean;
	}

}
