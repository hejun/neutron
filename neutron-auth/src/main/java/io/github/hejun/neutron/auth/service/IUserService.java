package io.github.hejun.neutron.auth.service;


import io.github.hejun.neutron.auth.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * 用户 Service
 *
 * @author HeJun
 */
public interface IUserService {

    Page<User> findPage(String username, Boolean enabled, Long tenantId, Pageable pageable);

    Optional<User> findByUsername(String username, Long tenantId);

    Optional<User> findById(Long id);

    User save(User user);

    void update(User user);

    void delete(Long id);

}
