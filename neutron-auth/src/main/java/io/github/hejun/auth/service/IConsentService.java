package io.github.hejun.auth.service;

import io.github.hejun.auth.entity.Consent;

/**
 * 客户端-用户授权 Service
 *
 * @author HeJun
 */
public interface IConsentService {

	Consent findByUserAndClient(Long userId, Long clientId);

	Consent save(Consent consent);

	long update(Consent consent);

	void delete(Long userId, Long clientId);

}
