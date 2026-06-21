package io.github.hejun.neutron.auth.service.impl;

import io.github.hejun.neutron.auth.entity.Tenant;
import io.github.hejun.neutron.auth.exception.OccupiedException;
import io.github.hejun.neutron.auth.repository.TenantRepository;
import io.github.hejun.neutron.auth.service.ITenantService;
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
import java.util.Optional;

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
    public Optional<Tenant> findByIssuer(String issuer) {
        if (!StringUtils.hasText(issuer)) {
            return Optional.empty();
        }
        return tenantRepository.findByIssuer(issuer);
    }

    @Override
    public Optional<Tenant> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return tenantRepository.findById(id);
    }

    @Override
    public Tenant save(Tenant tenant) {
        if (tenant == null || !StringUtils.hasText(tenant.getIssuer())) {
            return null;
        }
        if (tenantRepository.findByIssuer(tenant.getIssuer()).isPresent()) {
            throw new OccupiedException("租户地址：" + tenant.getIssuer() + " 已被使用");
        }
        tenant.setId(null);
        KeyPair keyPair = KEY_PAIR_GENERATOR.generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();
        tenant.setPublicKey(Base64.getEncoder().encodeToString(publicKey.getEncoded()));
        tenant.setPrivateKey(Base64.getEncoder().encodeToString(privateKey.getEncoded()));
        if (tenant.getEnabled() == null) {
            tenant.setEnabled(true);
        }
        return tenantRepository.save(tenant);
    }

    @Override
    public void update(Tenant tenant) {
        if (tenant == null || tenant.getId() == null) {
            return;
        }
        Optional<Tenant> checkerOptional = tenantRepository.findByIssuer(tenant.getIssuer());
        if (checkerOptional.isPresent() && !checkerOptional.get().getId().equals(tenant.getId())) {
            throw new OccupiedException("租户地址：" + tenant.getIssuer() + " 已被使用");
        }
        UpdateSpecification<Tenant> updateSpecification = UpdateSpecification
            .<Tenant>update((root, update, criteriaBuilder) -> {
                if (StringUtils.hasText(tenant.getName())) {
                    update.set("name", tenant.getName());
                }
                if (StringUtils.hasText(tenant.getIssuer())) {
                    update.set("issuer", tenant.getIssuer());
                }
                if (StringUtils.hasText(tenant.getCopyright())) {
                    update.set("copyright", tenant.getCopyright());
                }
                if (tenant.getEnabled() != null) {
                    update.set("enabled", tenant.getEnabled());
                }
            })
            .where((root, update, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("id"), tenant.getId())
            );
        tenantRepository.update(updateSpecification);
    }

    @Override
    public void delete(Long id) {
        if (id == null) {
            return;
        }
        tenantRepository.deleteById(id);
    }

}
