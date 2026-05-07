package io.github.hejun.neutron.gateway.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.util.List;

/**
 * @author HeJun
 */
@Getter
@Setter
@RefreshScope
@ConfigurationProperties(prefix = "neutron.security.oauth2.resourceserver.jwt")
public class SecurityProperties {

    private List<String> trustedIssuers;

}
