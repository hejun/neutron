package io.github.hejun.neutron.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * 用户扩展增强Service
 *
 * @author HeJun
 */
public interface UserDetailsEnhanceService extends UserDetailsService {

	UserDetails loadUserByPhone(String phone) throws UsernameNotFoundException;

}
