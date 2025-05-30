package io.github.hejun.neutron.properties.init;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 初始化用户
 *
 * @author HeJun
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "neutron.init.user")
public class InitializeUserProperties {

	private String username;
	private String password;

}
