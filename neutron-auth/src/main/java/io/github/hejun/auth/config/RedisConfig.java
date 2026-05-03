package io.github.hejun.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.security.jackson.SecurityJacksonModules;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

/**
 * Redis 配置
 *
 * @author HeJun
 */
@Configuration(proxyBeanMethods = false)
public class RedisConfig {

	@Bean
	public <T> RedisTemplate<String, T> redisTemplate(RedisConnectionFactory connectionFactory) {
		RedisSerializer<String> stringSerializer = RedisSerializer.string();

		String currentPkgName = this.getClass().getPackageName();
		BasicPolymorphicTypeValidator.Builder validator = BasicPolymorphicTypeValidator.builder()
			.allowIfSubType(currentPkgName.substring(0, currentPkgName.lastIndexOf(".") + 1));
		RedisSerializer<Object> jsonSerializer = GenericJacksonJsonRedisSerializer.create(it -> it
			.enableSpringCacheNullValueSupport()
			.enableUnsafeDefaultTyping()
			.customize(builder -> builder
				.addModules(SecurityJacksonModules.getModules(this.getClass().getClassLoader(), validator))
			)
		);

		RedisTemplate<String, T> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);
		template.setKeySerializer(stringSerializer);
		template.setValueSerializer(jsonSerializer);
		template.setHashKeySerializer(stringSerializer);
		template.setHashValueSerializer(jsonSerializer);
		template.afterPropertiesSet();
		return template;
	}

}
