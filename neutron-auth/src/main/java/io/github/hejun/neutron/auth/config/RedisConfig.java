package io.github.hejun.neutron.auth.config;

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
		RedisSerializer<String> keySerializer = RedisSerializer.string();

		String currentPkgName = this.getClass().getPackageName();
		BasicPolymorphicTypeValidator.Builder validator = BasicPolymorphicTypeValidator.builder()
            .allowIfBaseType("java.lang.")
			.allowIfSubType(currentPkgName.substring(0, currentPkgName.lastIndexOf(".") + 1));
		RedisSerializer<Object> valueSerializer = GenericJacksonJsonRedisSerializer.create(it -> it
			.enableSpringCacheNullValueSupport()
			.enableUnsafeDefaultTyping()
			.customize(builder -> builder
				.addModules(SecurityJacksonModules.getModules(this.getClass().getClassLoader(), validator))
			)
		);

		RedisTemplate<String, T> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);
		template.setKeySerializer(keySerializer);
		template.setValueSerializer(valueSerializer);
		template.setHashKeySerializer(keySerializer);
		template.setHashValueSerializer(valueSerializer);
		template.afterPropertiesSet();
		return template;
	}

}
