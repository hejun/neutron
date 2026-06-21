package io.github.hejun.neutron.fs.config;

import io.github.hejun.neutron.fs.properties.MinioProperties;
import io.minio.MinioAsyncClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Minio 配置
 *
 * @author HeJun
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(MinioProperties.class)
public class MinioConfig {

    @Bean
    public MinioAsyncClient minioAsyncClient(MinioProperties properties) {
        return MinioAsyncClient.builder()
            .endpoint(properties.getEndpoint())
            .credentials(properties.getAccessKey(), properties.getSecretKey())
            .build();
    }

}
