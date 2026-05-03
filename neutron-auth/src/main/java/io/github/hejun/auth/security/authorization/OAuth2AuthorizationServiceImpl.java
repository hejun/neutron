package io.github.hejun.auth.security.authorization;

import io.github.hejun.auth.security.authorization.redisdto.OAuth2AuthorizationCodeGrantAuthorization;
import io.github.hejun.auth.security.authorization.redisdto.OAuth2AuthorizationGrantAuthorization;
import io.github.hejun.auth.security.authorization.redisdto.OAuth2DeviceCodeGrantAuthorization;
import io.github.hejun.auth.security.authorization.redisdto.OidcAuthorizationCodeGrantAuthorization;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Function;

/**
 * 多租户 OAuth2AuthorizationService
 *
 * @author HeJun
 * @link <a href="https://docs.spring.io/spring-authorization-server/reference/guides/how-to-jpa.html">how-to-jpa</a>
 * @link <a href="https://docs.spring.io/spring-authorization-server/reference/guides/how-to-redis.html">how-to-redis</a>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthorizationServiceImpl implements OAuth2AuthorizationService {

	private final Function<String, String> KEY_FUN = (authorizationId) ->
		"neutron:auth:authorization:" + Optional.ofNullable(authorizationId).orElse("*");
	private final Function<String, String> KEY_INDEX_FUN = (key) ->
		"neutron:auth:authorization:index:" + key;

	private final JsonMapper jsonMapper;
	private final RegisteredClientRepository registeredClientRepository;
	private final RedisTemplate<String, OAuth2AuthorizationGrantAuthorization> dataRedisTemplate;
	private final RedisTemplate<String, String> stringRedisTemplate;

	@Override
	public void save(OAuth2Authorization authorization) {
		OAuth2AuthorizationGrantAuthorization redisAuthorization = OAuth2ModelMapper
			.convertOAuth2AuthorizationGrantAuthorization(authorization);
		if (redisAuthorization == null) {
			throw new AuthenticationServiceException("认证服务异常");
		}

		String key = KEY_FUN.apply(redisAuthorization.getId());
		// 默认5分钟过期
		Duration maxTimeout = Duration.ofMinutes(5);

		// 这里不用 Redis 提供的 CrudRepository 是因为他会建立一个反向索引, 不会过期!!!, 日积月累数据量会非常庞大, 所以自行建立一个反向索引, 用于加快查询
		if (AuthorizationGrantType.AUTHORIZATION_CODE.equals(authorization.getAuthorizationGrantType())) {
			OAuth2AuthorizationRequest authorizationRequest = authorization
				.getAttribute(OAuth2AuthorizationRequest.class.getName());
			if (authorizationRequest != null) {
				if (authorizationRequest.getState() != null) {
					String indexKey = KEY_INDEX_FUN.apply(authorizationRequest.getState());
					this.stringRedisTemplate.opsForValue().set(indexKey, key, maxTimeout);
				}
			}

			if (redisAuthorization.getAccessToken() != null) {
				Duration offsetTimeout = Duration.between(Instant.now(), redisAuthorization.getAccessToken().getExpiresAt());
				if (offsetTimeout.isPositive()) {
					maxTimeout = maxTimeout.compareTo(offsetTimeout) > 0 ? maxTimeout : offsetTimeout;
					String indexKey = KEY_INDEX_FUN.apply(redisAuthorization.getAccessToken().getTokenValue());
					this.stringRedisTemplate.opsForValue().set(indexKey, key, offsetTimeout);
				}
			}

			if (redisAuthorization.getRefreshToken() != null) {
				Duration offsetTimeout = Duration.between(Instant.now(), redisAuthorization.getRefreshToken().getExpiresAt());
				if (offsetTimeout.isPositive()) {
					maxTimeout = maxTimeout.compareTo(offsetTimeout) > 0 ? maxTimeout : offsetTimeout;
					String indexKey = KEY_INDEX_FUN.apply(redisAuthorization.getRefreshToken().getTokenValue());
					this.stringRedisTemplate.opsForValue().set(indexKey, key, offsetTimeout);
				}
			}

			if (redisAuthorization instanceof OAuth2AuthorizationCodeGrantAuthorization grantAuthorization) {
				if (grantAuthorization.getAuthorizationCode() != null) {
					Duration offsetTimeout = Duration.between(Instant.now(), grantAuthorization.getAuthorizationCode().getExpiresAt());
					if (offsetTimeout.isPositive()) {
						maxTimeout = maxTimeout.compareTo(offsetTimeout) > 0 ? maxTimeout : offsetTimeout;
						String indexKey = KEY_INDEX_FUN.apply(grantAuthorization.getAuthorizationCode().getTokenValue());
						this.stringRedisTemplate.opsForValue().set(indexKey, key, offsetTimeout);
					}
				}

				if (grantAuthorization instanceof OidcAuthorizationCodeGrantAuthorization oidcAuthorization) {
					if (oidcAuthorization.getState() != null) {
						String indexKey = KEY_INDEX_FUN.apply(oidcAuthorization.getState());
						this.stringRedisTemplate.opsForValue().set(indexKey, key, maxTimeout);
					}

					if (oidcAuthorization.getIdToken() != null) {
						Duration offsetTimeout = Duration.between(Instant.now(), oidcAuthorization.getIdToken().getExpiresAt());
						if (offsetTimeout.isPositive()) {
							maxTimeout = maxTimeout.compareTo(offsetTimeout) > 0 ? maxTimeout : offsetTimeout;
							String indexKey = KEY_INDEX_FUN.apply(oidcAuthorization.getIdToken().getTokenValue());
							this.stringRedisTemplate.opsForValue().set(indexKey, key, offsetTimeout);
						}
					}
				}

			}
		} else if (AuthorizationGrantType.DEVICE_CODE.equals(authorization.getAuthorizationGrantType()) &&
			redisAuthorization instanceof OAuth2DeviceCodeGrantAuthorization deviceCodeGrantAuthorization) {
			Duration offsetTimeout = Duration.between(Instant.now(), deviceCodeGrantAuthorization.getDeviceCode().getExpiresAt());
			if (offsetTimeout.isPositive()) {
				maxTimeout = maxTimeout.compareTo(offsetTimeout) > 0 ? maxTimeout : offsetTimeout;
				String indexKey = KEY_INDEX_FUN.apply(deviceCodeGrantAuthorization.getDeviceState());
				this.stringRedisTemplate.opsForValue().set(indexKey, key, offsetTimeout);
			}
		}

		this.dataRedisTemplate.opsForValue().set(key, redisAuthorization, maxTimeout);
	}

	@Override
	public void remove(OAuth2Authorization authorization) {
		String key = KEY_FUN.apply(authorization.getId());
		this.dataRedisTemplate.delete(key);
	}

	@Override
	public OAuth2Authorization findById(String id) {
		String key = KEY_FUN.apply(id);
		OAuth2AuthorizationGrantAuthorization redisAuthorization = this.dataRedisTemplate.opsForValue().get(key);
		if (redisAuthorization == null) {
			return null;
		}
		return this.toOAuth2Authorization(redisAuthorization);
	}

	@Override
	public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType) {
		String key = this.stringRedisTemplate.opsForValue().get(KEY_INDEX_FUN.apply(token));
		// 有索引走索引
		if (StringUtils.hasText(key)) {
			OAuth2AuthorizationGrantAuthorization authorization = this.dataRedisTemplate.opsForValue().get(key);
			if (authorization != null) {
				return this.toOAuth2Authorization(authorization);
			}
		}
		// 保底查询
		String multiGetKey = KEY_FUN.apply(null);
		try (Cursor<String> cursor = this.stringRedisTemplate.scan(ScanOptions.scanOptions().match(multiGetKey).build())) {
			while (cursor.hasNext()) {
				key = cursor.next();
				String redisAuthorization = this.stringRedisTemplate.opsForValue().get(key);
				if (redisAuthorization != null && redisAuthorization.contains(token)) {
					return toOAuth2Authorization(jsonMapper.readValue(redisAuthorization, OAuth2AuthorizationGrantAuthorization.class));
				}
			}
		}
		return null;
	}

	private OAuth2Authorization toOAuth2Authorization(
		OAuth2AuthorizationGrantAuthorization authorizationGrantAuthorization) {
		RegisteredClient registeredClient = this.registeredClientRepository
			.findById(authorizationGrantAuthorization.getRegisteredClientId());
		if (registeredClient == null) {
			return null;
		}
		OAuth2Authorization.Builder builder = OAuth2Authorization.withRegisteredClient(registeredClient);
		OAuth2ModelMapper.mapOAuth2AuthorizationGrantAuthorization(authorizationGrantAuthorization, builder);
		return builder.build();
	}


}
