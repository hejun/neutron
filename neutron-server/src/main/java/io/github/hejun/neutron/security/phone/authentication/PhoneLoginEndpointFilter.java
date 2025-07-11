package io.github.hejun.neutron.security.phone.authentication;

import org.springframework.http.HttpMethod;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

/**
 * 手机验证码登录 - 校验验证码 Filter
 *
 * @author HeJun
 */
public class PhoneLoginEndpointFilter extends AbstractAuthenticationProcessingFilter {

	public PhoneLoginEndpointFilter(String loginProcessingUrl) {
		super(PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, loginProcessingUrl));
		setAuthenticationConverter(new PhoneLoginAuthenticationConverter());
	}

}
