package io.github.hejun.auth.service.impl;

import io.github.hejun.auth.entity.User;
import io.github.hejun.auth.exception.OccupiedException;
import io.github.hejun.auth.repository.UserRepository;
import io.github.hejun.auth.service.IUserService;
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
	public User findByUsername(String username, Long tenantId) {
		if (tenantId == null || !StringUtils.hasText(username)) {
			return null;
		}
		return userRepository.findByUsernameAndTenantId(username, tenantId).orElse(null);
	}

	@Override
	public User findById(Long id) {
		if (id == null) {
			return null;
		}
		return userRepository.findById(id).orElse(null);
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
		return userRepository.save(user);
	}

	@Override
	public long update(User user) {
		if (user == null || user.getId() == null) {
			return 0;
		}

		UpdateSpecification<User> updateSpecification = UpdateSpecification
			.<User>update((root, update, criteriaBuilder) -> {
				if (StringUtils.hasText(user.getUsername())) {
					update.set("username", user.getUsername());
				}
				// 密码需要加密
				if (StringUtils.hasText(user.getPassword())) {
					update.set("password", passwordEncoder.encode(user.getPassword()));
				}
				if (user.getTenant() != null && user.getTenant().getId() != null) {
					update.set("tenantId", user.getTenant().getId());
				}
			})
			.where((root, update, criteriaBuilder) ->
				criteriaBuilder.equal(root.get("id"), user.getId())
			);
		return userRepository.update(updateSpecification);
	}

	@Override
	public void delete(Long id) {
		if (id == null) {
			return;
		}
		userRepository.deleteById(id);
	}

}
