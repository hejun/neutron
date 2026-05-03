package io.github.hejun.auth.service;


import io.github.hejun.auth.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 用户 Service
 *
 * @author HeJun
 */
public interface IUserService {

	Page<User> findPage(String username, Boolean enabled, Long tenantId, Pageable pageable);

	User findByUsername(String username, Long tenantId);

	User findById(Long id);

	User save(User user);

	long update(User user);

	void delete(Long id);

}
