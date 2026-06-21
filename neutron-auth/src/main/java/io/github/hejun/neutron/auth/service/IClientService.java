package io.github.hejun.neutron.auth.service;


import io.github.hejun.neutron.auth.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * 客户端 Service
 *
 * @author HeJun
 */
public interface IClientService {

    Page<Client> findPage(String clientId, String clientName, Boolean enabled, Long tenantId, Pageable pageable);

    Optional<Client> findById(Long id);

    Optional<Client> findByClientId(String clientId, Long tenantId);

    Client save(Client client);

    void update(Client client);

    void delete(Long id);

}
