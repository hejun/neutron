package io.github.hejun.neutron.security.phone.authentication;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.StringUtils;

/**
 * 手机验证码登录 - 检验验证码 AuthenticationConverter
 *
 * @author HeJun
 */
public class PhoneLoginAuthenticationConverter implements AuthenticationConverter {

	@Override
	public Authentication convert(HttpServletRequest request) {
		String phone = request.getParameter(OidcScopes.PHONE);
		String verifyCode = request.getParameter("verifyCode");
		if (!StringUtils.hasText(phone) || !StringUtils.hasText(verifyCode)) {
			return null;
		}
		return new PhoneLoginAuthenticationToken(phone, verifyCode);
	}

}
