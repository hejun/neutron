package io.github.hejun.neutron.config;

import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Web安全配置
 *
 * @author HeJun
 */
@EnableWebSecurity
@EnableMethodSecurity
@Configuration(proxyBeanMethods = false)
public class DefaultSecurityConfig {

	@Bean
	@Order(SecurityProperties.BASIC_AUTH_ORDER)
	public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
		http
			.authorizeHttpRequests(authorize ->
				authorize
					.requestMatchers("/style/**", "/script/**", "/favicon.ico", "/robots.txt").permitAll()
					.anyRequest().authenticated()
			)
			.formLogin(formLogin ->
				formLogin.loginPage("/login").permitAll()
			);
		return http.build();
	}

}
