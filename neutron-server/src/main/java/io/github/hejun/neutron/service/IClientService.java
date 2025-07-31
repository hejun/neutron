package io.github.hejun.neutron.service;

import io.github.hejun.neutron.entity.Client;

/**
 * 客户端 Service
 *
 * @author HeJun
 */
public interface IClientService {

	Client findById(Long id);

	Client findByClientId(Long tenantId, String clientId);

	Client save(Client client);

}
