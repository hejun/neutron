package io.github.hejun.neutron.repository;

import io.github.hejun.neutron.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * 租户 Repository
 *
 * @author HeJun
 */
@Repository
public interface TenantRepository extends JpaRepository<Tenant, String>, JpaSpecificationExecutor<Tenant> {
}
