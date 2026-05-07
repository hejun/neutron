package io.github.hejun.neutron.auth.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 初始化配置
 *
 * @author HeJun
 */
@ConfigurationProperties(prefix = "neutron.auth.init")
public record InitProperties(Tenant tenant, Client client, User user) {

    public record Tenant(String name, String issuer) {
    }

    public record Client(String clientId, String clientSecret, String clientName, List<String> redirectUris,
                         List<String> postLogoutRedirectUris) {
    }

    public record User(String username, String password, String nickname) {
    }

}
