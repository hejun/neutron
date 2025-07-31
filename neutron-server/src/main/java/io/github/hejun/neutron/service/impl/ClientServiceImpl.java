package io.github.hejun.neutron.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.hejun.neutron.entity.Client;
import io.github.hejun.neutron.mapper.ClientMapper;
import io.github.hejun.neutron.service.IClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 客户端 Service
 *
 * @author HeJun
 */
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ClientServiceImpl implements IClientService {

	private final ClientMapper clientMapper;

	@Override
	public Client findById(Long id) {
		if (id == null) {
			return null;
		}
		return clientMapper.selectById(id);
	}

	@Override
	public Client findByClientId(Long tenantId, String clientId) {
		if (tenantId == null || !StringUtils.hasText(clientId)) {
			return null;
		}
		return clientMapper.selectOne(Wrappers.<Client>lambdaQuery()
			.eq(Client::getTenantId, tenantId)
			.eq(Client::getClientId, clientId)
		);
	}

	@Override
	public Client save(Client client) {
		if (client == null || client.getTenantId() == null || !StringUtils.hasText(client.getClientId())) {
			return null;
		}
		clientMapper.insert(client);
		return client;
	}

}
