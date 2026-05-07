package io.github.hejun.neutron.auth.service;

import io.github.hejun.neutron.auth.entity.Consent;

import java.util.Optional;

/**
 * 客户端-用户授权 Service
 *
 * @author HeJun
 */
public interface IConsentService {

    Optional<Consent> findByUserAndClient(Long userId, Long clientId);

	Consent save(Consent consent);

    void update(Consent consent);

    void delete(Long userId, Long clientId);

}
