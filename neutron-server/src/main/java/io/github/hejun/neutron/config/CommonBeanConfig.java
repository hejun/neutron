package io.github.hejun.neutron.config;

import io.github.hejun.neutron.conveter.ClientConverter;
import io.github.hejun.neutron.conveter.UserConverter;
import io.github.hejun.neutron.entity.Client;
import io.github.hejun.neutron.entity.User;
import io.github.hejun.neutron.service.IClientService;
import io.github.hejun.neutron.service.IUserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 公用Bean配置
 *
 * @author HeJun
 */
@Configuration(proxyBeanMethods = false)
public class CommonBeanConfig {

	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	@Bean
	public UserDetailsService userDetailsService(IUserService userService,
												 UserConverter userConverter) {
		return username -> {
			User user = userService.findByUsername(username);
			if (user == null) {
				throw new UsernameNotFoundException(username);
			}
			return userConverter.toUserDetails(user);
		};
	}

	@Bean
	public RegisteredClientRepository registeredClientRepository(IClientService clientService,
																 ClientConverter clientConverter) {
		return new RegisteredClientRepository() {
			@Override
			public void save(RegisteredClient registeredClient) {
			}

			@Override
			public RegisteredClient findById(String id) {
				Client client = clientService.findById(id);
				if (client != null) {
					return clientConverter.toRegisteredClient(client);
				}
				return null;
			}

			@Override
			public RegisteredClient findByClientId(String clientId) {
				Client client = clientService.findByClientId(clientId);
				if (client != null) {
					return clientConverter.toRegisteredClient(client);
				}
				return null;
			}
		};
	}

	@Bean
	public OAuth2AuthorizationConsentService authorizationConsentService() {
		return new InMemoryOAuth2AuthorizationConsentService();
	}

	@Bean
	public CorsFilter corsFilter() {
		CorsConfiguration config = new CorsConfiguration();
		config.addAllowedOriginPattern("*");
		config.addAllowedMethod("*");
		config.addAllowedHeader("*");
		config.setMaxAge(1800L);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return new CorsFilter(source);
	}

}
