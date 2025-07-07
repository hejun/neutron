package io.github.hejun.neutron.security.issuer.context;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContext;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;

/**
 * 多租户 AuthorizationServerContext
 */
@Getter
@AllArgsConstructor
public final class DefaultAuthorizationServerContext implements AuthorizationServerContext {

	private final String issuer;
	private final String clientId;
	private final AuthorizationServerSettings authorizationServerSettings;

}
