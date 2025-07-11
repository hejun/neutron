package io.github.hejun.neutron.security.sendVerifyCode.authentication;

import io.github.hejun.neutron.security.sendVerifyCode.constants.VerifyCodeType;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.StringUtils;

/**
 * 发送验证码 AuthenticationConverter
 *
 * @author HeJun
 */
public class SendVerifyCodeAuthenticationConverter implements AuthenticationConverter {

	@Override
	public Authentication convert(HttpServletRequest request) {
		String phone = request.getParameter(OidcScopes.PHONE);
		String typeParameter = request.getParameter("type");

		VerifyCodeType type = null;
		if (StringUtils.hasText(typeParameter)) {
			type = VerifyCodeType.valueOf(typeParameter.toUpperCase());
		}
		return new SendVerifyCodeAuthenticationToken(phone, type);
	}

}
