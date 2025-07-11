package io.github.hejun.neutron.service.impl;

import io.github.hejun.neutron.service.UserDetailsEnhanceService;
import io.github.hejun.neutron.util.ContextUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 多租户用户Service
 *
 * @author HeJun
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UserDetailsServiceImpl implements UserDetailsEnhanceService {

	private final PasswordEncoder passwordEncoder;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		String issuer = ContextUtils.getIssuer();
		if (log.isDebugEnabled()) {
			log.debug("loadUserByUsername, current issuer: {}", issuer);
		}
		return this.mockFindUser(username);
	}

	@Override
	public UserDetails loadUserByPhone(String phone) throws UsernameNotFoundException {
		String issuer = ContextUtils.getIssuer();
		if (log.isDebugEnabled()) {
			log.debug("loadUserByPhone, current issuer: {}", issuer);
		}
		return this.mockFindUser(phone);
	}

	private UserDetails mockFindUser(String username) {
		return User.builder()
			.username(username)
			.password("1234")
			.roles("USER")
			.passwordEncoder(passwordEncoder::encode)
			.build();
	}
}
