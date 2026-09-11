package io.github.hejun.neutron.auth.repository;

import io.github.hejun.neutron.auth.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 角色 Repository
 *
 * @author HeJun
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long>, JpaSpecificationExecutor<Role> {

    Optional<Role> findByCodeAndTenantId(String code, Long tenantId);

}
