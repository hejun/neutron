package io.github.hejun.neutron.auth.service.impl;

import io.github.hejun.neutron.auth.entity.Tenant;
import io.github.hejun.neutron.auth.exception.OccupiedException;
import io.github.hejun.neutron.auth.repository.TenantRepository;
import io.github.hejun.neutron.auth.service.ITenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.*;
import java.util.Base64;
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

}
