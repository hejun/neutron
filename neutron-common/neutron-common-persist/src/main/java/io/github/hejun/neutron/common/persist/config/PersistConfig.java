package io.github.hejun.neutron.common.persist.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;


@Configuration(proxyBeanMethods = false)
@EnableJpaAuditing(modifyOnCreate = false)
public class PersistConfig {
}
