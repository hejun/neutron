package io.github.hejun.neutron.auth.security.util;

import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Issuer 转换提取器
 *
 * @author HeJun
 */
@Component
public class IssuerResolver {

    private final String issuer;
    private final Set<String> endpointUris;

    public IssuerResolver(AuthorizationServerSettings authorizationServerSettings) {
        if (authorizationServerSettings.getIssuer() != null) {
            this.issuer = authorizationServerSettings.getIssuer();
            this.endpointUris = Collections.emptySet();
        } else {
            this.issuer = null;
            this.endpointUris = new HashSet<>();
            this.endpointUris.add("/.well-known/oauth-authorization-server");
            this.endpointUris.add("/.well-known/openid-configuration");
            this.endpointUris.add("/consent");
            for (Map.Entry<String, Object> setting : authorizationServerSettings.getSettings().entrySet()) {
                if (setting.getKey().endsWith("-endpoint")) {
                    this.endpointUris.add((String) setting.getValue());
                }
            }
        }
    }

    public String resolve(String path) {
        if (this.issuer != null) {
            return this.issuer;
        }

        for (String endpointUri : endpointUris) {
            if (path.contains(endpointUri)) {
                return UriComponentsBuilder.fromUriString(path)
                    .replacePath(null)
                    .replaceQuery(null)
                    .build()
                    .toUriString();
            }
        }
        return null;
    }
}
