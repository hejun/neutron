package io.github.hejun.auth.service;


import io.github.hejun.auth.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 客户端 Service
 *
 * @author HeJun
 */
public interface IClientService {

	Page<Client> findPage(String name, Boolean enabled, Long tenantId, Pageable pageable);

	Client findById(Long id);

	Client findByClientId(String clientId, Long tenantId);

	Client save(Client client);

	long update(Client client);

	void delete(Long id);

}
