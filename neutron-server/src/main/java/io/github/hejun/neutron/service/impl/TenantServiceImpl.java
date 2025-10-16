package io.github.hejun.neutron.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.hejun.neutron.entity.Tenant;
import io.github.hejun.neutron.mapper.TenantMapper;
import io.github.hejun.neutron.service.ITenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Objects;

/**
 * 租户 Service
 *
 * @author HeJun
 */
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class TenantServiceImpl implements ITenantService {

	private final TenantMapper tenantMapper;

	@Override
	public IPage<Tenant> findPage(Long current, Long size, String name, Boolean enabled) {
		return tenantMapper.selectPage(Page.of(current, size), Wrappers.<Tenant>lambdaQuery()
			.select(Tenant::getId, Tenant::getName, Tenant::getIssuer, Tenant::getEnabled, Tenant::getCreateDate)
			.like(StringUtils.hasText(name), Tenant::getName, name)
			.eq(Objects.nonNull(enabled), Tenant::getEnabled, enabled)
		);
	}

	@Override
	@Cacheable(cacheNames = "tenant:issuer", key = "#issuer?:''", unless = "#result == null")
	public Tenant findByIssuer(String issuer) {
		if (!StringUtils.hasText(issuer)) {
			return null;
		}
		return tenantMapper.selectOne(Wrappers.<Tenant>lambdaQuery().eq(Tenant::getIssuer, issuer));
	}

	@Override
	public Tenant findById(Long id) {
		if (id == null) {
			return null;
		}
		return tenantMapper.selectById(id);
	}

	@Override
	public Tenant save(Tenant tenant) {
		if (tenant == null || !StringUtils.hasText(tenant.getIssuer())) {
			return null;
		}
		KeyPair keyPair = this.generateKeyPair();
		PublicKey publicKey = keyPair.getPublic();
		PrivateKey privateKey = keyPair.getPrivate();
		tenant.setPublicKey(Base64.getEncoder().encodeToString(publicKey.getEncoded()));
		tenant.setPrivateKey(Base64.getEncoder().encodeToString(privateKey.getEncoded()));
		tenantMapper.insert(tenant);
		return tenant;
	}

	@Override
	@CacheEvict(cacheNames = "tenant:issuer", key = "#tenant.issuer?:''")
	public Tenant update(Tenant tenant) {
		if (tenant == null || tenant.getId() == null){
			return null;
		}
		// 不允许更新公私钥
		tenant.setPublicKey(null);
		tenant.setPrivateKey(null);
		tenantMapper.updateById(tenant);
		return tenant;
	}

	private KeyPair generateKeyPair() {
		KeyPair keyPair;
		try {
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			keyPairGenerator.initialize(2048);
			keyPair = keyPairGenerator.generateKeyPair();
		} catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
		return keyPair;
	}

}
