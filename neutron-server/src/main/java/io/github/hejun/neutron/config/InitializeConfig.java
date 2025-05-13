package io.github.hejun.neutron.config;

import io.github.hejun.neutron.entity.Tenant;
import io.github.hejun.neutron.properties.init.InitializeTenantProperties;
import io.github.hejun.neutron.service.ITenantService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;

/**
 * 初始化配置
 *
 * @author HeJun
 */
@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@ConfigurationPropertiesScan(basePackages = {"io.github.hejun.neutron.properties"})
public class InitializeConfig implements ApplicationListener<ApplicationStartedEvent> {

	private final InitializeTenantProperties initializeTenantProperties;
	private final ITenantService tenantService;

	@Override
	public void onApplicationEvent(ApplicationStartedEvent event) {
		String tenantCode = initializeTenantProperties.getCode();
		if (StringUtils.isNotBlank(tenantCode)) {
			Tenant exist = tenantService.findByCode(tenantCode);
			// 如果不存在, 说明没有初始化过, 需要初始化
			if (exist == null) {
				Tenant tenant = new Tenant();
				tenant.setCode(initializeTenantProperties.getCode());
				tenant.setName(initializeTenantProperties.getName());
				tenantService.save(tenant);
			}
		}
	}

}
