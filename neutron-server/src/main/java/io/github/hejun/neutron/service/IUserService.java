package io.github.hejun.neutron.service;

import io.github.hejun.neutron.entity.Client;
import io.github.hejun.neutron.entity.User;
import io.github.hejun.neutron.entity.UserClient;

import java.util.List;

/**
 * 用户 Service
 *
 * @author HeJun
 */
public interface IUserService {

	User findByUsername(Long tenantId, String username);

	User save(User user);

	List<Client> findUserClients(Long userId);

	UserClient saveUserClient(Long userId, Long clientId);

}
