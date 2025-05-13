package io.github.hejun.neutron.properties.init;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 初始化租户
 *
 * @author HeJun
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "neutron.init.tenant")
public class InitializeTenantProperties {

	private String code;
	private String name;

}
