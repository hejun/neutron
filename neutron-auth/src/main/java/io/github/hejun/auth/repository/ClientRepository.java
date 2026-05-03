package io.github.hejun.auth.repository;

import io.github.hejun.auth.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 客户端 Repository
 *
 * @author HeJun
 */
@Repository
public interface ClientRepository extends JpaRepository<Client, Long>, JpaSpecificationExecutor<Client> {

	Optional<Client> findByClientIdAndTenantId(String clientId, Long tenantId);

}
