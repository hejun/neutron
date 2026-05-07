package io.github.hejun.neutron.auth.service;


import io.github.hejun.neutron.auth.entity.User;

import java.util.Optional;

/**
 * 用户 Service
 *
 * @author HeJun
 */
public interface IUserService {

	Optional<User> findByUsername(String username, Long tenantId);

	Optional<User> findById(Long id);

	User save(User user);

}
