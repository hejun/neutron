package io.github.hejun.neutron.common.otlp.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.opentelemetry.autoconfigure.OpenTelemetrySdkAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenTelemetryAppender 配置
 *
 * @author HeJun
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(OpenTelemetrySdkAutoConfiguration.class)
public class OTLPConfig {

	@Bean
	@ConditionalOnBean(OpenTelemetry.class)
	public InitializingBean installOpenTelemetryAppender(OpenTelemetry openTelemetry) {
		return () -> OpenTelemetryAppender.install(openTelemetry);
	}

}
