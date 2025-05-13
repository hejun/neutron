package io.github.hejun.neutron.service;

import io.github.hejun.neutron.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 客户端 Service
 *
 * @author HeJun
 */
public interface IClientService {

	Page<Client> findPage(Client client, Pageable pageable);

	Client findById(String id);

	Client findByClientId(String clientId);

	Client save(Client client);

	Client update(Client client);

	void deleteById(String id);

}
