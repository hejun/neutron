package io.github.hejun.neutron.security.sendVerifyCode.authentication;

import io.github.hejun.neutron.security.sendVerifyCode.constants.VerifyCodeType;
import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Collections;

/**
 * 发送验证码 AuthenticationToken
 *
 * @author HeJun
 */
public class SendVerifyCodeAuthenticationToken extends AbstractAuthenticationToken {

	private final Object principal;
	@Getter
	private final VerifyCodeType type;
	@Getter
	private Long expiresIn;
	@Getter
	private Long invalidIn;

	public SendVerifyCodeAuthenticationToken(Object principal, VerifyCodeType type) {
		super(Collections.emptyList());
		this.principal = principal;
		this.type = type;
		setAuthenticated(false);
	}

	public SendVerifyCodeAuthenticationToken(Object principal, VerifyCodeType type, Long expiresIn, Long invalidIn) {
		super(Collections.emptyList());
		this.principal = principal;
		this.type = type;
		this.expiresIn = expiresIn;
		this.invalidIn = invalidIn;
		setAuthenticated(true);
	}

	@Override
	public Object getPrincipal() {
		return this.principal;
	}

	@Override
	public Object getCredentials() {
		return null;
	}

}
