package io.github.hejun.neutron.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nonnull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.redis.core.RedisKeyValueAdapter;
import org.springframework.data.redis.core.convert.Jsr310Converters;
import org.springframework.data.redis.core.convert.RedisCustomConversions;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.server.authorization.jackson2.OAuth2AuthorizationServerJackson2Module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Redis 配置
 *
 * @author HeJun
 */
@Configuration(proxyBeanMethods = false)
@EnableRedisRepositories(enableKeyspaceEvents = RedisKeyValueAdapter.EnableKeyspaceEvents.ON_STARTUP)
public class RedisConfig {

	@Bean
	public RedisCustomConversions redisCustomConversions() {
		List<Converter<?, ?>> converters = new ArrayList<>();
		converters.addAll(Jsr310Converters.getConvertersToRegister());
		converters.addAll(Arrays.asList(
			new UsernamePasswordAuthenticationTokenToBytesConverter(), new BytesToUsernamePasswordAuthenticationTokenConverter(),
			new OAuth2AuthorizationRequestToBytesConverter(), new BytesToOAuth2AuthorizationRequestConverter()
		));
		return new RedisCustomConversions(converters);
	}

	@ReadingConverter
	public static class BytesToOAuth2AuthorizationRequestConverter implements Converter<byte[], OAuth2AuthorizationRequest> {

		private final Jackson2JsonRedisSerializer<OAuth2AuthorizationRequest> serializer;

		public BytesToOAuth2AuthorizationRequestConverter() {
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.registerModules(
				SecurityJackson2Modules.getModules(BytesToOAuth2AuthorizationRequestConverter.class.getClassLoader()));
			objectMapper.registerModule(new OAuth2AuthorizationServerJackson2Module());
			this.serializer = new Jackson2JsonRedisSerializer<>(objectMapper, OAuth2AuthorizationRequest.class);
		}

		@Override
		public OAuth2AuthorizationRequest convert(@Nonnull byte[] value) {
			return this.serializer.deserialize(value);
		}

	}

	@ReadingConverter
	public static class BytesToUsernamePasswordAuthenticationTokenConverter
		implements Converter<byte[], UsernamePasswordAuthenticationToken> {

		private final Jackson2JsonRedisSerializer<UsernamePasswordAuthenticationToken> serializer;

		public BytesToUsernamePasswordAuthenticationTokenConverter() {
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.registerModules(SecurityJackson2Modules
				.getModules(BytesToUsernamePasswordAuthenticationTokenConverter.class.getClassLoader()));
			this.serializer = new Jackson2JsonRedisSerializer<>(objectMapper, UsernamePasswordAuthenticationToken.class);
		}

		@Override
		public UsernamePasswordAuthenticationToken convert(@Nonnull byte[] value) {
			return this.serializer.deserialize(value);
		}

	}

	@WritingConverter
	public static class OAuth2AuthorizationRequestToBytesConverter implements Converter<OAuth2AuthorizationRequest, byte[]> {

		private final Jackson2JsonRedisSerializer<OAuth2AuthorizationRequest> serializer;

		public OAuth2AuthorizationRequestToBytesConverter() {
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.registerModules(
				SecurityJackson2Modules.getModules(OAuth2AuthorizationRequestToBytesConverter.class.getClassLoader()));
			objectMapper.registerModules(new OAuth2AuthorizationServerJackson2Module());
			this.serializer = new Jackson2JsonRedisSerializer<>(objectMapper, OAuth2AuthorizationRequest.class);
		}

		@Override
		public byte[] convert(@Nonnull OAuth2AuthorizationRequest value) {
			return this.serializer.serialize(value);
		}

	}

	@WritingConverter
	public static class UsernamePasswordAuthenticationTokenToBytesConverter
		implements Converter<UsernamePasswordAuthenticationToken, byte[]> {

		private final Jackson2JsonRedisSerializer<UsernamePasswordAuthenticationToken> serializer;

		public UsernamePasswordAuthenticationTokenToBytesConverter() {
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.registerModules(SecurityJackson2Modules
				.getModules(UsernamePasswordAuthenticationTokenToBytesConverter.class.getClassLoader()));
			this.serializer = new Jackson2JsonRedisSerializer<>(objectMapper, UsernamePasswordAuthenticationToken.class);
		}

		@Override
		public byte[] convert(@Nonnull UsernamePasswordAuthenticationToken value) {
			return this.serializer.serialize(value);
		}

	}

}
