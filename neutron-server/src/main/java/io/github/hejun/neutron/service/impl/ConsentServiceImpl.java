package io.github.hejun.neutron.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.hejun.neutron.entity.Consent;
import io.github.hejun.neutron.mapper.ConsentMapper;
import io.github.hejun.neutron.service.IConsentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 客户端-用户授权 Service
 *
 * @author HeJun
 */
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ConsentServiceImpl implements IConsentService {

	private final ConsentMapper consentMapper;

	@Override
	public Consent findByClientAndUser(Long clientId, Long userId) {
		return consentMapper.selectOne(Wrappers.<Consent>lambdaQuery()
			.eq(Consent::getClientId, clientId)
			.eq(Consent::getUserId, userId)
		);
	}

	@Override
	public Consent save(Consent consent) {
		consentMapper.insert(consent);
		return consent;
	}

	@Override
	public Consent update(Consent consent) {
		if (consent.getClientId() == null || consent.getUserId() == null) {
			return consent;
		}
		consentMapper.update(Wrappers.<Consent>lambdaUpdate()
			.set(Consent::getAuthorities, consent.getAuthorities())
			.eq(Consent::getClientId, consent.getClientId())
			.eq(Consent::getUserId, consent.getUserId())
		);
		return consent;
	}

	@Override
	public void delete(Consent consent) {
		consentMapper.delete(Wrappers.<Consent>lambdaQuery()
			.eq(Consent::getClientId, consent.getClientId())
			.eq(Consent::getUserId, consent.getUserId())
		);
	}

}
