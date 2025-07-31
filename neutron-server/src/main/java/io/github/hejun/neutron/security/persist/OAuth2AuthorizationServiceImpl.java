package io.github.hejun.neutron.security.persist;

import io.github.hejun.neutron.util.ContextUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 多租户 OAuth2AuthorizationService
 *
 * @author HeJun
 * @link <a href="https://docs.spring.io/spring-authorization-server/reference/guides/how-to-jpa.html">how-to-jpa</a>
 * @link <a href="https://docs.spring.io/spring-authorization-server/reference/guides/how-to-redis.html">how-to-redis</a>
 */
@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class OAuth2AuthorizationServiceImpl implements OAuth2AuthorizationService {

	private final io.github.hejun.neutron.security.persist.OAuth2AuthorizationModelMapper OAuth2AuthorizationModelMapper;
	private final RedisAuthorizationRepository redisAuthorizationRepository;

	@Override
	public void save(OAuth2Authorization authorization) {
		String issuer = ContextUtils.getIssuer();
		if (log.isDebugEnabled()) {
			log.debug("save, current issuer: {}", issuer);
		}
		OAuth2AuthorizationModelMapper.RedisAuthorization redisAuthorization = OAuth2AuthorizationModelMapper.convert(authorization);
		this.redisAuthorizationRepository.save(redisAuthorization);
	}

	@Override
	public void remove(OAuth2Authorization authorization) {
		String issuer = ContextUtils.getIssuer();
		if (log.isDebugEnabled()) {
			log.debug("remove, current issuer: {}", issuer);
		}
		OAuth2AuthorizationModelMapper.RedisAuthorization redisAuthorization = this.OAuth2AuthorizationModelMapper.convert(authorization);
		this.redisAuthorizationRepository.delete(redisAuthorization);
	}

	@Override
	public OAuth2Authorization findById(String id) {
		String issuer = ContextUtils.getIssuer();
		if (log.isDebugEnabled()) {
			log.debug("findById, current issuer: {}", issuer);
		}
		return this.redisAuthorizationRepository.findById(id).map(OAuth2AuthorizationModelMapper::convert).orElse(null);
	}

	@Override
	public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType) {
		String issuer = ContextUtils.getIssuer();
		if (log.isDebugEnabled()) {
			log.debug("findByToken, current issuer: {}", issuer);
		}
		if (tokenType == null) {
			Optional<OAuth2AuthorizationModelMapper.RedisAuthorization> authorizationOptional = this.redisAuthorizationRepository
				.findByStateOrAuthorizationCode_TokenValue(token, token);
			if (authorizationOptional.isEmpty()) {
				authorizationOptional = this.redisAuthorizationRepository
					.findByAccessToken_TokenValueOrRefreshToken_TokenValue(token, token);
			}
			if (authorizationOptional.isEmpty()) {
				authorizationOptional = this.redisAuthorizationRepository
					.findByIdToken_TokenValue(token);
			}
			if (authorizationOptional.isEmpty()) {
				authorizationOptional = this.redisAuthorizationRepository
					.findByStateOrDeviceCode_TokenValueOrUserCode_TokenValue(token, token, token);
			}
			if (authorizationOptional.isPresent()) {
				return authorizationOptional.map(OAuth2AuthorizationModelMapper::convert).orElse(null);
			}
		} else if (OAuth2ParameterNames.STATE.equals(tokenType.getValue())) {
			return this.redisAuthorizationRepository.findByState(token).map(OAuth2AuthorizationModelMapper::convert).orElse(null);
		} else if (OAuth2ParameterNames.CODE.equals(tokenType.getValue())) {
			return this.redisAuthorizationRepository.findByAuthorizationCode_TokenValue(token).map(OAuth2AuthorizationModelMapper::convert).orElse(null);
		} else if (OAuth2TokenType.ACCESS_TOKEN.equals(tokenType)) {
			return this.redisAuthorizationRepository.findByAccessToken_TokenValue(token).map(OAuth2AuthorizationModelMapper::convert).orElse(null);
		} else if (OAuth2TokenType.REFRESH_TOKEN.equals(tokenType)) {
			return this.redisAuthorizationRepository.findByRefreshToken_TokenValue(token).map(OAuth2AuthorizationModelMapper::convert).orElse(null);
		} else if (OidcParameterNames.ID_TOKEN.equals(tokenType.getValue())) {
			return this.redisAuthorizationRepository.findByIdToken_TokenValue(token).map(OAuth2AuthorizationModelMapper::convert).orElse(null);
		} else if (OAuth2ParameterNames.DEVICE_CODE.equals(tokenType.getValue())) {
			return this.redisAuthorizationRepository.findByDeviceCode_TokenValue(token).map(OAuth2AuthorizationModelMapper::convert).orElse(null);
		} else if (OAuth2ParameterNames.USER_CODE.equals(tokenType.getValue())) {
			return this.redisAuthorizationRepository.findByUserCode_TokenValue(token).map(OAuth2AuthorizationModelMapper::convert).orElse(null);
		}
		return null;
	}

}
