package io.github.hejun.neutron.service;

import io.github.hejun.neutron.entity.Tenant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 租户 Service
 *
 * @author HeJun
 */
public interface ITenantService {

	Page<Tenant> findPage(Tenant tenant, Pageable pageable);

	Tenant findById(String id);

	Tenant save(Tenant tenant);

	Tenant update(Tenant tenant);

	void deleteById(String id);

}
