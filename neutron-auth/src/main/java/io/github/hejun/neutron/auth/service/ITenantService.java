package io.github.hejun.neutron.auth.service;

import io.github.hejun.neutron.auth.entity.Tenant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * 租户 Service
 *
 * @author HeJun
 */
public interface ITenantService {

    Page<Tenant> findPage(String name, Boolean enabled, Pageable pageable);

	Optional<Tenant> findByIssuer(String issuer);

	Optional<Tenant> findById(Long id);

	Tenant save(Tenant tenant);

    void update(Tenant tenant);

    void delete(Long id);

}
