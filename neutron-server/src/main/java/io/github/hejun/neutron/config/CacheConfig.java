package io.github.hejun.neutron.config;

import io.github.hejun.neutron.constant.Constants;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * 缓存配置
 *
 * @author HeJun
 */
@EnableCaching
@Configuration(proxyBeanMethods = false)
public class CacheConfig {

	@Bean
	public RedisCacheConfiguration redisCacheConfiguration(CacheProperties cacheProperties) {
		CacheProperties.Redis redisProperties = cacheProperties.getRedis();
		RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
			.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string()))
			.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.json()))
			.computePrefixWith(name -> name + Constants.COLON);
		if (redisProperties.getTimeToLive() != null) {
			config = config.entryTtl(redisProperties.getTimeToLive());
		}
		if (redisProperties.getKeyPrefix() != null) {
			config = config.computePrefixWith(name -> redisProperties.getKeyPrefix() + Constants.COLON + name + Constants.COLON);
		}
		if (!redisProperties.isCacheNullValues()) {
			config = config.disableCachingNullValues();
		}
		if (!redisProperties.isUseKeyPrefix()) {
			config = config.disableKeyPrefix();
		}
		return config;
	}

}
