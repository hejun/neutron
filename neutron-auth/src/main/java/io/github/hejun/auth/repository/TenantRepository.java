package io.github.hejun.auth.repository;

import io.github.hejun.auth.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 租户 Repository
 *
 * @author HeJun
 */
@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long>, JpaSpecificationExecutor<Tenant> {

	Optional<Tenant> findByIssuer(String issuer);

}
