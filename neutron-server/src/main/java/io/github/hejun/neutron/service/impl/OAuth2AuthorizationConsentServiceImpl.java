package io.github.hejun.neutron.service.impl;

import io.github.hejun.neutron.util.ContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsent;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.stereotype.Component;

/**
 * 多租户授权确认Service
 *
 * @author HeJun
 */
@Slf4j
@Component
public class OAuth2AuthorizationConsentServiceImpl implements OAuth2AuthorizationConsentService {

	private final OAuth2AuthorizationConsentService delegateOAuth2AuthorizationConsentService;

	public OAuth2AuthorizationConsentServiceImpl() {
		this.delegateOAuth2AuthorizationConsentService = new InMemoryOAuth2AuthorizationConsentService();
	}

	@Override
	public void save(OAuth2AuthorizationConsent authorizationConsent) {
		String issuer = ContextUtils.getIssuer();
		if (log.isDebugEnabled()) {
			log.debug("save, current issuer: {}", issuer);
		}
		delegateOAuth2AuthorizationConsentService.save(authorizationConsent);
	}

	@Override
	public void remove(OAuth2AuthorizationConsent authorizationConsent) {
		String issuer = ContextUtils.getIssuer();
		if (log.isDebugEnabled()) {
			log.debug("remove, current issuer: {}", issuer);
		}
		delegateOAuth2AuthorizationConsentService.remove(authorizationConsent);
	}

	@Override
	public OAuth2AuthorizationConsent findById(String registeredClientId, String principalName) {
		String issuer = ContextUtils.getIssuer();
		if (log.isDebugEnabled()) {
			log.debug("findById, current issuer: {}", issuer);
		}
		return delegateOAuth2AuthorizationConsentService.findById(registeredClientId, principalName);
	}

}
