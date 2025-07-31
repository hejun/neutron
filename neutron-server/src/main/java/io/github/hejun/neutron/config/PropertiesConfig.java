package io.github.hejun.neutron.config;

import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Configuration;

/**
 * Properties 配置
 *
 * @author HeJun
 */
@Configuration(proxyBeanMethods = false)
@ConfigurationPropertiesScan(basePackages = "io.github.hejun.neutron.properties")
public class PropertiesConfig {
}
