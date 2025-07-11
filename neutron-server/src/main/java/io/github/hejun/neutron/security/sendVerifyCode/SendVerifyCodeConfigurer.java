package io.github.hejun.neutron.security.sendVerifyCode;

import io.github.hejun.neutron.security.sendVerifyCode.authentication.SendVerifyCodeAuthenticationConverter;
import io.github.hejun.neutron.security.sendVerifyCode.authentication.SendVerifyCodeAuthenticationProvider;
import io.github.hejun.neutron.security.sendVerifyCode.authentication.SendVerifyCodeEndpointFilter;
import io.github.hejun.neutron.service.SMSService;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * 手机验证码登录 Configurer
 *
 * @author HeJun
 */
public class SendVerifyCodeConfigurer<H extends HttpSecurityBuilder<H>>
	extends AbstractHttpConfigurer<SendVerifyCodeConfigurer<H>, H> {

	private RequestMatcher requestMatcher;
	private AuthenticationConverter authenticationConverter;
	private Long expireIn;
	private Long invalidIn;

	@Override
	public void init(H builder) throws Exception {
		if (requestMatcher == null) {
			requestMatcher = PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.GET, "/sendVerifyCode");
		}
		if (authenticationConverter == null) {
			authenticationConverter = new SendVerifyCodeAuthenticationConverter();
		}
		if (expireIn == null) {
			expireIn = 60L;
		}
		if (invalidIn == null) {
			invalidIn = 300L;
		}
	}

	@Override
	public void configure(H http) throws Exception {
		AuthenticationManager authenticationManager = this.getBean(http, AuthenticationManager.class);
		SendVerifyCodeEndpointFilter sendVerifyCodeEndpointFilter = new SendVerifyCodeEndpointFilter();
		sendVerifyCodeEndpointFilter.setRequestMatcher(requestMatcher);
		sendVerifyCodeEndpointFilter.setAuthenticationConverter(authenticationConverter);
		sendVerifyCodeEndpointFilter.setAuthenticationManager(authenticationManager);
		http.addFilterAfter(postProcess(sendVerifyCodeEndpointFilter), AbstractPreAuthenticatedProcessingFilter.class);

		OAuth2AuthorizationService authorizationService = this.getBean(http, OAuth2AuthorizationService.class);
		RegisteredClientRepository registeredClientRepository = this.getBean(http, RegisteredClientRepository.class);
		SMSService smsService = this.getBean(http, SMSService.class);
		SendVerifyCodeAuthenticationProvider authenticationProvider =
			new SendVerifyCodeAuthenticationProvider(authorizationService, registeredClientRepository, smsService);
		if (expireIn != null && expireIn > 0) {
			authenticationProvider.setExpireIn(expireIn);
		}
		if (invalidIn != null && invalidIn > 0) {
			authenticationProvider.setInvalidIn(invalidIn);
		}
		http.authenticationProvider(authenticationProvider);
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
