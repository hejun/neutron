package io.github.hejun.neutron.service.impl;

import io.github.hejun.neutron.entity.User;
import io.github.hejun.neutron.exception.StatefulRuntimeException;
import io.github.hejun.neutron.repository.UserRepository;
import io.github.hejun.neutron.service.IUserService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 用户Service
 *
 * @author HeJun
 */
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class UserServiceImpl implements IUserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	public Page<User> findPage(User user, Pageable pageable) {
		Specification<User> spec = (root, query, criteriaBuilder) -> {
			List<Predicate> predicates = new ArrayList<>();
			if (user != null) {
				if (StringUtils.isNotBlank(user.getUsername())) {
					predicates.add(criteriaBuilder.like(root.get("username"), "%" + user.getUsername() + "%"));
				}
				if (user.getEnabled() != null) {
					predicates.add(criteriaBuilder.equal(root.get("enabled"), user.getEnabled()));
				}
			}
			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		};
		pageable = PageRequest
			.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.Direction.DESC, "createDate");
		return userRepository.findAll(spec, pageable);
	}

	@Override
	public User findById(String id) {
		if (StringUtils.isBlank(id)) {
			return null;
		}
		return userRepository.findById(id).orElse(null);
	}

	@Override
	public User findByUsername(String username) {
		if (StringUtils.isBlank(username)) {
			return null;
		}
		return userRepository.findByUsername(username).orElse(null);
	}

	@Override
	public User save(User user) {
		if (user == null) {
			return null;
		}
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		if (user.getEnabled() == null) {
			user.setEnabled(true);
		}
		user.setCreateDate(new Date());
		user.setLastModifiedDate(null);
		return userRepository.save(user);
	}

	@Override
	public User update(User user) {
		User exists;
		if (user == null || (exists = this.findById(user.getId())) == null) {
			return null;
		}
		if (StringUtils.isNotBlank(user.getUsername())) {
			exists.setUsername(user.getUsername());
		}
		if (user.getEnabled() != null) {
			exists.setEnabled(user.getEnabled());
		}
		exists.setLastModifiedDate(new Date());
		return userRepository.save(exists);
	}

	@Override
	public User updatePassword(String id, String oldPassword, String newPassword) {
		if (StringUtils.isBlank(id) || StringUtils.isBlank(oldPassword) || StringUtils.isBlank(newPassword)) {
			return null;
		}
		User user = this.findById(id);
		if (user == null) {
			return null;
		}
		if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
			throw new StatefulRuntimeException(400, "old password is not match");
		}
		user.setPassword(passwordEncoder.encode(newPassword));
		return userRepository.save(user);
	}

	@Override
	public void deleteById(String id) {
		if (StringUtils.isNotBlank(id)) {
			userRepository.deleteById(id);
		}
	}

}
