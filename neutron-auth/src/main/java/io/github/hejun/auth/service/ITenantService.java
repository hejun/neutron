package io.github.hejun.auth.service;

import io.github.hejun.auth.entity.Tenant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 租户 Service
 *
 * @author HeJun
 */
public interface ITenantService {

	Page<Tenant> findPage(String name, Boolean enabled, Pageable pageable);

	Tenant findByIssuer(String issuer);

	Tenant findById(Long id);

	Tenant save(Tenant tenant);

	long update(Tenant tenant);

	void delete(Long id);


}
