package io.github.hejun.neutron.security.component;

import io.github.hejun.neutron.util.ContextUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.stereotype.Component;

/**
 * 多租户 OAuth2AuthorizationService
 *
 * @author HeJun
 * @link <a href="https://docs.spring.io/spring-authorization-server/reference/guides/how-to-jpa.html">how-to-jpa</a>
 * @link <a href="https://docs.spring.io/spring-authorization-server/reference/guides/how-to-redis.html">how-to-redis</a>
 */
@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class OAuth2AuthorizationServiceImpl implements OAuth2AuthorizationService {

	private final OAuth2AuthorizationService authorizationService = new InMemoryOAuth2AuthorizationService();

	@Override
	public void save(OAuth2Authorization authorization) {
		String issuer = ContextUtil.getIssuer();
		if (log.isDebugEnabled()) {
			log.debug("save, current issuer: {}", issuer);
		}
		authorizationService.save(authorization);
	}

	@Override
	public void remove(OAuth2Authorization authorization) {
		String issuer = ContextUtil.getIssuer();
		if (log.isDebugEnabled()) {
			log.debug("remove, current issuer: {}", issuer);
		}
		authorizationService.remove(authorization);
	}

	@Override
	public OAuth2Authorization findById(String id) {
		String issuer = ContextUtil.getIssuer();
		if (log.isDebugEnabled()) {
			log.debug("findById, current issuer: {}", issuer);
		}
		return authorizationService.findById(id);
	}

	@Override
	public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType) {
		String issuer = ContextUtil.getIssuer();
		if (log.isDebugEnabled()) {
			log.debug("findByToken, current issuer: {}", issuer);
		}
		return authorizationService.findByToken(token, tokenType);
	}
}
