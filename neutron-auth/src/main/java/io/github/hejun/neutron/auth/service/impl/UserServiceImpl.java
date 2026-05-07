package io.github.hejun.neutron.auth.service.impl;

import io.github.hejun.neutron.auth.entity.User;
import io.github.hejun.neutron.auth.exception.OccupiedException;
import io.github.hejun.neutron.auth.repository.UserRepository;
import io.github.hejun.neutron.auth.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * 用户 Service
 *
 * @author HeJun
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;

    @Override
    public Optional<User> findByUsername(String username, Long tenantId) {
        if (tenantId == null || !StringUtils.hasText(username)) {
            return Optional.empty();
        }
        return userRepository.findByUsernameAndTenantId(username, tenantId);
    }

    @Override
    public Optional<User> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return userRepository.findById(id);
    }

    @Override
    public User save(User user) {
        if (user == null || user.getTenant() == null || user.getTenant().getId() == null || !StringUtils.hasText(user.getUsername())) {
            return null;
        }
        if (userRepository.findByUsernameAndTenantId(user.getUsername(), user.getTenant().getId()).isPresent()) {
            throw new OccupiedException("用户名：" + user.getUsername() + " 已被使用");
        }
        // 密码需要加密
        if (StringUtils.hasText(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        if (user.getEmailVerified() == null) {
            user.setEmailVerified(false);
        }
        if (user.getPhoneNumberVerified() == null) {
            user.setPhoneNumberVerified(false);
        }
        if (user.getEnabled() == null) {
            user.setEnabled(true);
        }
        return userRepository.save(user);
    }

}
