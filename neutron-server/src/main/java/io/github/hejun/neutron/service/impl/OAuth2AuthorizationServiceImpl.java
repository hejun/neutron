package io.github.hejun.neutron.service.impl;

import io.github.hejun.neutron.util.ContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.stereotype.Component;

/**
 * 多租户 OAuth2AuthorizationService
 *
 * @author HeJun
 */
@Slf4j
@Component
public class OAuth2AuthorizationServiceImpl implements OAuth2AuthorizationService {

	private final OAuth2AuthorizationService delegateOAuth2AuthorizationService;

	public OAuth2AuthorizationServiceImpl() {
		this.delegateOAuth2AuthorizationService = new InMemoryOAuth2AuthorizationService();
	}

	@Override
	public void save(OAuth2Authorization authorization) {
		String issuer = ContextUtils.getIssuer();
		if (log.isDebugEnabled()) {
			log.debug("save, current issuer: {}", issuer);
		}
		this.delegateOAuth2AuthorizationService.save(authorization);
	}

	@Override
	public void remove(OAuth2Authorization authorization) {
		String issuer = ContextUtils.getIssuer();
		if (log.isDebugEnabled()) {
			log.debug("remove, current issuer: {}", issuer);
		}
		this.delegateOAuth2AuthorizationService.remove(authorization);
	}

	@Override
	public OAuth2Authorization findById(String id) {
		String issuer = ContextUtils.getIssuer();
		if (log.isDebugEnabled()) {
			log.debug("findById, current issuer: {}", issuer);
		}
		return this.delegateOAuth2AuthorizationService.findById(id);
	}

	@Override
	public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType) {
		String issuer = ContextUtils.getIssuer();
		if (log.isDebugEnabled()) {
			log.debug("findByToken, current issuer: {}", issuer);
		}
		return this.delegateOAuth2AuthorizationService.findByToken(token, tokenType);
	}

}
