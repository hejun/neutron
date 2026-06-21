package io.github.hejun.neutron.auth.service.impl;

import io.github.hejun.neutron.auth.entity.User;
import io.github.hejun.neutron.auth.exception.OccupiedException;
import io.github.hejun.neutron.auth.repository.UserRepository;
import io.github.hejun.neutron.auth.service.IUserService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.UpdateSpecification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
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
    public Page<User> findPage(String username, Boolean enabled, Long tenantId, Pageable pageable) {
        Specification<User> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(username)) {
                predicates.add(cb.like(root.get("username"), "%" + username + "%"));
            }
            if (enabled != null) {
                predicates.add(cb.equal(root.get("enabled"), enabled));
            }
            if (tenantId != null) {
                predicates.add(cb.equal(root.get("tenant").get("id"), tenantId));
            }
            return cb.and(predicates);
        };
        return userRepository.findAll(specification, pageable);
    }

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

    @Override
    public void update(User user) {
        if (user == null || user.getId() == null) {
            return;
        }
        Optional<User> checkerOptional = userRepository.findByUsernameAndTenantId(user.getUsername(), user.getTenant().getId());
        if (checkerOptional.isPresent() && !checkerOptional.get().getId().equals(user.getId())) {
            throw new OccupiedException("用户名：" + user.getUsername() + " 已被使用");
        }
        UpdateSpecification<User> updateSpecification = UpdateSpecification
            .<User>update((root, update, criteriaBuilder) -> {
                if (StringUtils.hasText(user.getUsername())) {
                    update.set("username", user.getUsername());
                }
                if (StringUtils.hasText(user.getPassword())) {
                    update.set("password", passwordEncoder.encode(user.getPassword()));
                }
                if (StringUtils.hasText(user.getEmail())) {
                    update.set("email", user.getEmail());
                }
                if (user.getEmailVerified() != null) {
                    update.set("emailVerified", user.getEmailVerified());
                }
                if (StringUtils.hasText(user.getPhoneNumber())) {
                    update.set("phoneNumber", user.getPhoneNumber());
                }
                if (user.getPhoneNumberVerified() != null) {
                    update.set("phoneNumberVerified", user.getPhoneNumberVerified());
                }
                if (StringUtils.hasText(user.getNickname())) {
                    update.set("nickname", user.getNickname());
                }
                if (user.getGender() != null) {
                    update.set("gender", user.getGender());
                }
                if (StringUtils.hasText(user.getAvatar())) {
                    update.set("avatar", user.getAvatar());
                }
                if (user.getBirthdate() != null) {
                    update.set("birthdate", user.getBirthdate());
                }
                if (user.getEnabled() != null) {
                    update.set("enabled", user.getEnabled());
                }
                if (user.getTenant() != null && user.getTenant().getId() != null) {
                    update.set("tenant", user.getTenant());
                }
            })
            .where((root, update, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("id"), user.getId())
            );
        userRepository.update(updateSpecification);
    }

    @Override
    public void delete(Long id) {
        if (id == null) {
            return;
        }
        userRepository.deleteById(id);
    }

}
