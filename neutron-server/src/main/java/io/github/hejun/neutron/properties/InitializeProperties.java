package io.github.hejun.neutron.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 初始化 Properties
 *
 * @author HeJun
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "neutron.bootstrap")
public class InitializeProperties {

	private String adminTenantName;
	private String adminTenantIssuer;
	private String adminClientId;
	private String adminClientName;
	private List<String> adminClientRedirectUris;
	private String adminUsername;
	private String adminPassword;

}
