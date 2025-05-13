package io.github.hejun.neutron.repository;

import io.github.hejun.neutron.entity.Client;
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
public interface ClientRepository extends JpaRepository<Client, String>, JpaSpecificationExecutor<Client> {

	Optional<Client> findByClientId(String clientId);

}
