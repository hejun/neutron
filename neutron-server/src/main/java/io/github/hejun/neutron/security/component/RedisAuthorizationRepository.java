package io.github.hejun.neutron.security.component;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Redis 存储映射 Repository
 */
@Repository
public interface RedisAuthorizationRepository extends CrudRepository<OAuth2AuthorizationModelMapper.RedisAuthorization, String> {

	Optional<OAuth2AuthorizationModelMapper.RedisAuthorization> findByState(String state);

	Optional<OAuth2AuthorizationModelMapper.RedisAuthorization> findByAuthorizationCode_TokenValue(String tokenValue);

	Optional<OAuth2AuthorizationModelMapper.RedisAuthorization> findByAccessToken_TokenValue(String tokenValue);

	Optional<OAuth2AuthorizationModelMapper.RedisAuthorization> findByRefreshToken_TokenValue(String tokenValue);

	Optional<OAuth2AuthorizationModelMapper.RedisAuthorization> findByIdToken_TokenValue(String tokenValue);

	Optional<OAuth2AuthorizationModelMapper.RedisAuthorization> findByDeviceCode_TokenValue(String tokenValue);

	Optional<OAuth2AuthorizationModelMapper.RedisAuthorization> findByUserCode_TokenValue(String tokenValue);

	Optional<OAuth2AuthorizationModelMapper.RedisAuthorization> findByStateOrAuthorizationCode_TokenValue(String state, String tokenValue);

	Optional<OAuth2AuthorizationModelMapper.RedisAuthorization> findByAccessToken_TokenValueOrRefreshToken_TokenValue(String accessTokenValue, String refreshTokenValue);

	Optional<OAuth2AuthorizationModelMapper.RedisAuthorization> findByStateOrDeviceCode_TokenValueOrUserCode_TokenValue(String state, String deviceCodeValue, String userCodeTokenValue);

}
