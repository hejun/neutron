package io.github.hejun.neutron.auth.service.impl;

import io.github.hejun.neutron.auth.entity.Client;
import io.github.hejun.neutron.auth.exception.OccupiedException;
import io.github.hejun.neutron.auth.repository.ClientRepository;
import io.github.hejun.neutron.auth.service.IClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * 客户端 Service
 *
 * @author HeJun
 */
@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements IClientService {

    private final PasswordEncoder passwordEncoder;
	private final ClientRepository clientRepository;

	@Override
	public Optional<Client> findById(Long id) {
		if (id == null) {
			return Optional.empty();
		}
		return clientRepository.findById(id);
	}

	@Override
	public Optional<Client> findByClientId(String clientId, Long tenantId) {
		if (tenantId == null || !StringUtils.hasText(clientId)) {
			return Optional.empty();
		}
		return clientRepository.findByClientIdAndTenantId(clientId, tenantId);
	}

	@Override
	public Client save(Client client) {
		if (client == null || client.getTenant() == null || client.getTenant().getId() == null || !StringUtils.hasText(client.getClientId())) {
			return null;
		}
		if (clientRepository.findByClientIdAndTenantId(client.getClientId(), client.getTenant().getId()).isPresent()) {
			throw new OccupiedException("客户端：" + client.getClientId() + " 已被使用");
		}
        // 密码需要加密
        if (StringUtils.hasText(client.getClientSecret())) {
            client.setClientSecret(passwordEncoder.encode(client.getClientSecret()));
        }
        if (client.getEnabled() == null) {
            client.setEnabled(true);
        }
		return clientRepository.save(client);
	}

}
