package io.github.hejun.neutron.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.hejun.neutron.entity.*;
import io.github.hejun.neutron.mapper.*;
import io.github.hejun.neutron.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户 Service
 *
 * @author HeJun
 */
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UserServiceImpl implements IUserService {

	private final UserMapper userMapper;
	private final ClientMapper clientMapper;
	private final UserClientMapper userClientMapper;

	@Override
	public User findByUsername(Long tenantId, String username) {
		if (tenantId == null || !StringUtils.hasText(username)) {
			return null;
		}
		return userMapper.selectOne(Wrappers.<User>lambdaQuery()
			.eq(User::getTenantId, tenantId)
			.eq(User::getUsername, username)
		);
	}

	@Override
	public User save(User user) {
		if (user == null || user.getTenantId() == null || !StringUtils.hasText(user.getUsername())) {
			return null;
		}
		userMapper.insert(user);
		return user;
	}

	@Override
	public List<Client> findUserClients(Long userId) {
		if (userId == null) {
			return Collections.emptyList();
		}
		List<UserClient> userClientList = userClientMapper
			.selectList(Wrappers.<UserClient>lambdaQuery().eq(UserClient::getUserId, userId));
		if (!CollectionUtils.isEmpty(userClientList)) {
			Set<Long> clientIds = userClientList.stream()
				.map(UserClient::getClientId)
				.collect(Collectors.toSet());
			if (!CollectionUtils.isEmpty(clientIds)) {
				return clientMapper.selectByIds(clientIds);
			}
		}
		return List.of();
	}

	@Override
	public UserClient saveUserClient(Long userId, Long clientId) {
		if (userId == null || clientId == null) {
			return null;
		}
		UserClient userClient = userClientMapper.selectOne(Wrappers.<UserClient>lambdaQuery()
			.eq(UserClient::getUserId, userId)
			.eq(UserClient::getClientId, clientId)
		);
		if (userClient == null) {
			userClient = new UserClient();
			userClient.setClientId(clientId);
			userClient.setUserId(userId);
			userClientMapper.insert(userClient);
		}
		return userClient;
	}

}
