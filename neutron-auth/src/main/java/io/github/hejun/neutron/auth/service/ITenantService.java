package io.github.hejun.neutron.auth.service;

import io.github.hejun.neutron.auth.entity.Tenant;

import java.util.Optional;

/**
 * 租户 Service
 *
 * @author HeJun
 */
public interface ITenantService {

	Optional<Tenant> findByIssuer(String issuer);

	Optional<Tenant> findById(Long id);

	Tenant save(Tenant tenant);

}
