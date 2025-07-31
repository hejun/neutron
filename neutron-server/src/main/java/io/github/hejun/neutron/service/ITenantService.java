package io.github.hejun.neutron.service;

import io.github.hejun.neutron.entity.Tenant;

/**
 * 租户 Service
 *
 * @author HeJun
 */
public interface ITenantService {

	Tenant findByIssuer(String issuer);

	Tenant save(Tenant tenant);

}
