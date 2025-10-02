package io.github.hejun.neutron.security.component;

import io.github.hejun.neutron.util.ContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsent;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.stereotype.Component;

/**
 * 多租户 OAuth2AuthorizationConsentService
 *
 * @author HeJun
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class OAuth2AuthorizationConsentServiceImpl implements OAuth2AuthorizationConsentService {

	private final OAuth2AuthorizationConsentService oAuth2AuthorizationConsentService = new InMemoryOAuth2AuthorizationConsentService();

	@Override
	public void save(OAuth2AuthorizationConsent authorizationConsent) {
		String issuer = ContextUtil.getIssuer();
		if (log.isDebugEnabled()) {
			log.debug("save, current issuer: {}", issuer);
		}
		oAuth2AuthorizationConsentService.save(authorizationConsent);
	}

	@Override
	public void remove(OAuth2AuthorizationConsent authorizationConsent) {
		String issuer = ContextUtil.getIssuer();
		if (log.isDebugEnabled()) {
			log.debug("remove, current issuer: {}", issuer);
		}
		oAuth2AuthorizationConsentService.remove(authorizationConsent);
	}

	@Override
	public OAuth2AuthorizationConsent findById(String registeredClientId, String principalName) {
		String issuer = ContextUtil.getIssuer();
		if (log.isDebugEnabled()) {
			log.debug("findById, current issuer: {}", issuer);
		}
		return oAuth2AuthorizationConsentService.findById(registeredClientId, principalName);
	}

}
