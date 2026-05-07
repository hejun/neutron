package io.github.hejun.neutron.auth.service;


import io.github.hejun.neutron.auth.entity.Client;

import java.util.Optional;

/**
 * 客户端 Service
 *
 * @author HeJun
 */
public interface IClientService {

    Optional<Client> findById(Long id);

	Optional<Client> findByClientId(String clientId, Long tenantId);

	Client save(Client client);

}
