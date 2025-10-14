package io.github.hejun.neutron.service;

import io.github.hejun.neutron.entity.Consent;

/**
 * 客户端-用户授权 Service
 *
 * @author HeJun
 */
public interface IConsentService {

	Consent findByClientAndUser(Long clientId, Long userId);

	Consent save(Consent consent);

	Consent update(Consent consent);

	void delete(Consent consent);

}
