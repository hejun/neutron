package io.github.hejun.neutron.security.phone.authentication;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;

/**
 * 手机验证码登录 - 检验验证码 AuthenticationConverter
 *
 * @author HeJun
 */
public class PhoneLoginAuthenticationToken extends AbstractAuthenticationToken {

	private final Object principal;
	private final Object credentials;

	public PhoneLoginAuthenticationToken(Object principal, Object credentials) {
		super(Collections.emptyList());
		this.principal = principal;
		this.credentials = credentials;
		setAuthenticated(false);
	}

	public PhoneLoginAuthenticationToken(Object principal, Collection<? extends GrantedAuthority> authorities) {
		super(authorities);
		this.principal = principal;
		this.credentials = null;
		setAuthenticated(true);
	}

	@Override
	public Object getPrincipal() {
		return principal;
	}

	@Override
	public Object getCredentials() {
		return credentials;
	}

}
