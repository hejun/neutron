package io.github.hejun.neutron.security.component;

import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * RedisAuthorization 和 OAuth2Authorization 转换类
 *
 * @author HeJun
 */
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class OAuth2AuthorizationModelMapper {

	private final RegisteredClientRepository registeredClientRepository;

	public RedisAuthorization convert(OAuth2Authorization authorization) {
		RedisAuthorization redisAuthorization = new RedisAuthorization();
		redisAuthorization.setId(authorization.getId());
		redisAuthorization.setRegisteredClientId(authorization.getRegisteredClientId());
		redisAuthorization.setPrincipalName(authorization.getPrincipalName());
		redisAuthorization.setAuthorizationGrantType(authorization.getAuthorizationGrantType().getValue());
		redisAuthorization.setAuthorizedScopes(authorization.getAuthorizedScopes());
		redisAuthorization.setAttributes(convertUnmodifiableMap(authorization.getAttributes()));
		if (authorization.getAttribute(OAuth2ParameterNames.STATE) != null) {
			redisAuthorization.setState(authorization.getAttribute(OAuth2ParameterNames.STATE));
		}

		if (authorization.getToken(OAuth2AuthorizationCode.class) != null) {
			OAuth2Authorization.Token<OAuth2AuthorizationCode> authorizationCode = authorization.getToken(OAuth2AuthorizationCode.class);
			redisAuthorization.setAuthorizationCode(convertToken(authorizationCode));
			this.handleTimeToLive(redisAuthorization, authorizationCode);
		}
		if (authorization.getAccessToken() != null) {
			OAuth2Authorization.Token<OAuth2AccessToken> accessToken = authorization.getAccessToken();
			redisAuthorization.setAccessToken(convertToken(accessToken, accessToken.getToken().getScopes()));
			this.handleTimeToLive(redisAuthorization, accessToken);
		}
		if (authorization.getRefreshToken() != null) {
			OAuth2Authorization.Token<OAuth2RefreshToken> refreshToken = authorization.getRefreshToken();
			redisAuthorization.setRefreshToken(convertToken(refreshToken));
			this.handleTimeToLive(redisAuthorization, refreshToken);
		}
		if (authorization.getToken(OidcIdToken.class) != null) {
			OAuth2Authorization.Token<OidcIdToken> oidcIdToken = authorization.getToken(OidcIdToken.class);
			redisAuthorization.setIdToken(convertToken(oidcIdToken, Optional.ofNullable(oidcIdToken).map(OAuth2Authorization.Token::getClaims).orElse(null)));
			this.handleTimeToLive(redisAuthorization, oidcIdToken);
		}
		if (authorization.getToken(OAuth2DeviceCode.class) != null) {
			OAuth2Authorization.Token<OAuth2DeviceCode> deviceCodeToken = authorization.getToken(OAuth2DeviceCode.class);
			redisAuthorization.setDeviceCode(convertToken(deviceCodeToken));
			this.handleTimeToLive(redisAuthorization, deviceCodeToken);
		}
		if (authorization.getToken(OAuth2UserCode.class) != null) {
			OAuth2Authorization.Token<OAuth2UserCode> userCodeToken = authorization.getToken(OAuth2UserCode.class);
			redisAuthorization.setUserCode(convertToken(userCodeToken));
			this.handleTimeToLive(redisAuthorization, userCodeToken);
		}

		this.handleTimeToLive(redisAuthorization, null);
		return redisAuthorization;
	}

	public OAuth2Authorization convert(RedisAuthorization redisAuthorization) {
		if (redisAuthorization == null) {
			return null;
		}

		RegisteredClient registeredClient = registeredClientRepository.findById(redisAuthorization.getRegisteredClientId());
		if (registeredClient == null) {
			throw new OAuth2AuthenticationException("Registered client not found");
		}
		OAuth2Authorization.Builder builder = OAuth2Authorization.withRegisteredClient(registeredClient)
			.id(redisAuthorization.getId())
			.principalName(redisAuthorization.getPrincipalName())
			.authorizationGrantType(new AuthorizationGrantType(redisAuthorization.getAuthorizationGrantType()))
			.authorizedScopes(redisAuthorization.getAuthorizedScopes())
			.attributes(attributes -> Optional.ofNullable(redisAuthorization.getAttributes()).ifPresent(attributes::putAll));

		if (redisAuthorization.getAuthorizationCode() != null) {
			RedisToken token = redisAuthorization.getAuthorizationCode();
			builder.token(new OAuth2AuthorizationCode(token.getTokenValue(), token.getIssuedAt(), token.getExpiresAt()), metadata -> Optional.ofNullable(token.getMetadata()).ifPresent(metadata::putAll));
		}
		if (redisAuthorization.getAccessToken() != null) {
			RedisToken token = redisAuthorization.getAccessToken();
			builder.token(new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, token.getTokenValue(), token.getIssuedAt(), token.getExpiresAt(), token.getScopes()), metadata -> Optional.ofNullable(token.getMetadata()).ifPresent(metadata::putAll));
		}
		if (redisAuthorization.getRefreshToken() != null) {
			RedisToken token = redisAuthorization.getRefreshToken();
			builder.token(new OAuth2RefreshToken(token.getTokenValue(), token.getIssuedAt(), token.getExpiresAt()), metadata -> Optional.ofNullable(token.getMetadata()).ifPresent(metadata::putAll));
		}
		if (redisAuthorization.getIdToken() != null) {
			RedisToken token = redisAuthorization.getIdToken();
			builder.token(new OidcIdToken(token.getTokenValue(), token.getIssuedAt(), token.getExpiresAt(), token.getClaims().getClaims()), metadata -> Optional.ofNullable(token.getMetadata()).ifPresent(metadata::putAll));
		}
		if (redisAuthorization.getDeviceCode() != null) {
			RedisToken token = redisAuthorization.getDeviceCode();
			builder.token(new OAuth2DeviceCode(token.getTokenValue(), token.getIssuedAt(), token.getExpiresAt()), metadata -> Optional.ofNullable(token.getMetadata()).ifPresent(metadata::putAll));
		}
		if (redisAuthorization.getUserCode() != null) {
			RedisToken token = redisAuthorization.getUserCode();
			builder.token(new OAuth2UserCode(token.getTokenValue(), token.getIssuedAt(), token.getExpiresAt()), metadata -> Optional.ofNullable(token.getMetadata()).ifPresent(metadata::putAll));
		}
		return builder.build();
	}

	private RedisToken convertToken(OAuth2Authorization.Token<?> token) {
		if (token == null) {
			return null;
		}
		OAuth2Token oAuth2Token = token.getToken();
		RedisToken redisToken = new RedisToken();
		redisToken.setTokenValue(oAuth2Token.getTokenValue());
		redisToken.setIssuedAt(oAuth2Token.getIssuedAt());
		redisToken.setExpiresAt(oAuth2Token.getExpiresAt());
		redisToken.setMetadata(convertUnmodifiableMap(token.getMetadata()));
		return redisToken;
	}

	private RedisToken convertToken(OAuth2Authorization.Token<?> token, Set<String> accessTokenScopes) {
		RedisToken redisToken = this.convertToken(token);
		redisToken.setScopes(new HashSet<>(accessTokenScopes));
		return redisToken;
	}

	private RedisToken convertToken(OAuth2Authorization.Token<?> token, Map<String, Object> claims) {
		RedisToken redisToken = this.convertToken(token);
		redisToken.setClaims(new ClaimsHolder(claims));
		return redisToken;
	}

	private Map<String, Object> convertUnmodifiableMap(Map<String, Object> map) {
		if (map == null) {
			return null;
		}
		Map<String, Object> unmodifiableMap = new HashMap<>();
		if (!map.isEmpty()) {
			for (Map.Entry<String, Object> entry : map.entrySet()) {
				switch (entry.getValue()) {
					case Map<?, ?> mapValue -> unmodifiableMap.put(entry.getKey(), new HashMap<>(mapValue));
					case List<?> listValue -> unmodifiableMap.put(entry.getKey(), new ArrayList<>(listValue));
					case URL url -> unmodifiableMap.put(entry.getKey(), url.toString());
					case null, default -> unmodifiableMap.put(entry.getKey(), entry.getValue());
				}
			}
		}
		return unmodifiableMap;
	}

	private void handleTimeToLive(RedisAuthorization redisAuthorization, OAuth2Authorization.Token<?> token) {
		if (redisAuthorization == null) {
			return;
		}
		if (token != null && token.getToken() != null && token.getToken().getExpiresAt() != null) {
			redisAuthorization.setTimeToLive(Duration.between(Instant.now(), token.getToken().getExpiresAt()).getSeconds());
		}
		if (redisAuthorization.getTimeToLive() == null) {
			redisAuthorization.setTimeToLive(TimeUnit.MINUTES.toSeconds(5));
		}
	}

	/**
	 * OAuth2Authorization 存储映射实体
	 *
	 * @author HeJun
	 */
	@Getter
	@Setter
	@RedisHash("authorization")
	@NoArgsConstructor
	@AllArgsConstructor
	public static class RedisAuthorization {

		@Id
		private String id;
		private String registeredClientId;
		private String principalName;
		private String authorizationGrantType;
		private Set<String> authorizedScopes;
		private RedisToken authorizationCode;
		private RedisToken accessToken;
		private RedisToken refreshToken;
		private RedisToken idToken;
		private RedisToken deviceCode;
		private RedisToken userCode;
		private Map<String, Object> attributes;

		@Indexed
		private String state;
		@TimeToLive
		private Long timeToLive;
	}

	/**
	 * OAuth2Token 存储映射实体
	 *
	 * @author HeJun
	 */
	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class RedisToken {

		@Indexed
		private String tokenValue;
		private Instant issuedAt;
		private Instant expiresAt;
		private boolean invalidated;

		private OAuth2AccessToken.TokenType tokenType;
		private Set<String> scopes;
		private Map<String, Object> metadata;
		private ClaimsHolder claims;

	}


	@Getter
	@Setter
	public static class ClaimsHolder {

		private final Map<String, Object> claims;

		public ClaimsHolder(Map<String, Object> claims) {
			this.claims = claims;
		}

	}

}
