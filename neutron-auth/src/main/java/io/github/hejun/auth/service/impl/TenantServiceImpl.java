package io.github.hejun.auth.service.impl;

import io.github.hejun.auth.entity.Tenant;
import io.github.hejun.auth.exception.OccupiedException;
import io.github.hejun.auth.repository.TenantRepository;
import io.github.hejun.auth.service.ITenantService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.UpdateSpecification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * 租户 Service
 *
 * @author HeJun
 */
@Service
@RequiredArgsConstructor
public class TenantServiceImpl implements ITenantService {

	private final TenantRepository tenantRepository;

	private static final KeyPairGenerator KEY_PAIR_GENERATOR;

	static {
		try {
			KEY_PAIR_GENERATOR = KeyPairGenerator.getInstance("RSA");
			KEY_PAIR_GENERATOR.initialize(2048, SecureRandom.getInstanceStrong());
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("RSA KeyPairGenerator 初始化失败", ex);
		}
	}

	@Override
	public Page<Tenant> findPage(String name, Boolean enabled, Pageable pageable) {
		Specification<Tenant> specification = (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();
			if (StringUtils.hasText(name)) {
				predicates.add(cb.like(root.get("name"), "%" + name + "%"));
			}
			if (enabled != null) {
				predicates.add(cb.equal(root.get("enabled"), enabled));
			}
			return cb.and(predicates);
		};
		return tenantRepository.findAll(specification, pageable);
	}

	@Override
	public Tenant findByIssuer(String issuer) {
		if (!StringUtils.hasText(issuer)) {
			return null;
		}
		return tenantRepository.findByIssuer(issuer).orElse(null);
	}

	@Override
	public Tenant findById(Long id) {
		if (id == null) {
			return null;
		}
		return tenantRepository.findById(id).orElse(null);
	}

	@Override
	public Tenant save(Tenant tenant) {
		if (tenant == null || !StringUtils.hasText(tenant.getIssuer())) {
			return null;
		}
		if (tenantRepository.findByIssuer(tenant.getIssuer()).isPresent()) {
			throw new OccupiedException("租户地址：" + tenant.getIssuer() + " 已被使用");
		}
		KeyPair keyPair = KEY_PAIR_GENERATOR.generateKeyPair();
		PublicKey publicKey = keyPair.getPublic();
		PrivateKey privateKey = keyPair.getPrivate();
		tenant.setPublicKey(Base64.getEncoder().encodeToString(publicKey.getEncoded()));
		tenant.setPrivateKey(Base64.getEncoder().encodeToString(privateKey.getEncoded()));
		return tenantRepository.save(tenant);
	}

	@Override
	public long update(Tenant tenant) {
		if (tenant == null || tenant.getId() == null) {
			return 0;
		}

		UpdateSpecification<Tenant> updateSpecification = UpdateSpecification
			.<Tenant>update((root, update, criteriaBuilder) -> {
				if (StringUtils.hasText(tenant.getIssuer())) {
					update.set("issuer", tenant.getIssuer());
				}
			})
			.where((root, update, criteriaBuilder) ->
				criteriaBuilder.equal(root.get("id"), tenant.getId())
			);
		return tenantRepository.update(updateSpecification);
	}

	@Override
	public void delete(Long id) {
		if (id == null) {
			return;
		}
		tenantRepository.deleteById(id);
	}

}
