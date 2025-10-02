package io.github.hejun.neutron.config;

import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 默认安全配置
 *
 * @author HeJun
 */
@EnableWebSecurity
@EnableMethodSecurity
@Configuration(proxyBeanMethods = false)
public class DefaultSecurityConfig {

	@Bean
	@Order(SecurityProperties.BASIC_AUTH_ORDER)
	public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
		http
			.authorizeHttpRequests(authorize ->
				authorize
					.requestMatchers("/assets/**", "/favicon.ico", "/robots.txt", "/.well-known/**").permitAll()
					.anyRequest().authenticated()
			)
			.cors(Customizer.withDefaults())
			.formLogin(formLogin ->
				formLogin
					.loginPage("/login").permitAll()
					.loginProcessingUrl("/login/account")
			)
			.logout(logout ->
				logout
					.logoutRequestMatcher(this.createLogoutRequestMatcher())
					.defaultLogoutSuccessHandlerFor(this.createLogoutSuccessHandler(),
						PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.GET, "/logout"))
			)
			.oauth2ResourceServer(oauth2ResourceServer ->
				oauth2ResourceServer
					.jwt(Customizer.withDefaults())
			);
		return http.build();
	}

	private RequestMatcher createLogoutRequestMatcher() {
		String logoutUrl = "/logout";

		PathPatternRequestMatcher post = PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, logoutUrl);
		PathPatternRequestMatcher get = PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.GET, logoutUrl);

		return new OrRequestMatcher(get, post);
	}

	private LogoutSuccessHandler createLogoutSuccessHandler() {
		String logoutSuccessUrl = "/logout?logout";

		SimpleUrlLogoutSuccessHandler urlLogoutHandler = new SimpleUrlLogoutSuccessHandler();
		urlLogoutHandler.setUseReferer(true);
		urlLogoutHandler.setDefaultTargetUrl(logoutSuccessUrl);
		return urlLogoutHandler;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	@Bean
	public CorsFilter corsFilter() {
		CorsConfiguration config = new CorsConfiguration();
		config.addAllowedOriginPattern("*");
		config.addAllowedMethod("*");
		config.addAllowedHeader("*");
		config.setMaxAge(1800L);
		config.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return new CorsFilter(source);
	}

}
