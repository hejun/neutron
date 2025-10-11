package io.github.hejun.neutron.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.github.hejun.neutron.entity.Tenant;

/**
 * 租户 Service
 *
 * @author HeJun
 */
public interface ITenantService {

	IPage<Tenant> findPage(Long current, Long size, String name, Boolean enabled);

	Tenant findByIssuer(String issuer);

	Tenant save(Tenant tenant);

}
