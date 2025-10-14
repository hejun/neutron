package io.github.hejun.neutron.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 初始化 Properties
 *
 * @author HeJun
 */
@ConfigurationProperties(prefix = "init")
public record InitializeProperties(Tenant tenant, Client client, User user) {

	public record Tenant(String name, String issuer) {
	}

	public record Client(String clientId, String name, List<String> redirectUris) {
	}

	public record User(String name, String password) {
	}

}
