package io.github.hejun.neutron.service;

import io.github.hejun.neutron.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 用户 Service
 *
 * @author HeJun
 */
public interface IUserService {

	Page<User> findPage(User user, Pageable pageable);

	User findById(String id);

	User findByUsername(String username);

	User save(User user);

	User update(User user);

	User updatePassword(String id, String oldPassword, String newPassword);

	void deleteById(String id);

}
