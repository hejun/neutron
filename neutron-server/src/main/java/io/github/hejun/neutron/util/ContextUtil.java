package io.github.hejun.neutron.util;

import io.github.hejun.neutron.security.issuer.context.DefaultAuthorizationServerContext;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContext;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;

/**
 * ContextUtil
 *
 * @author HeJun
 */
public class ContextUtil {

	public static String getIssuer() {
		AuthorizationServerContext context = AuthorizationServerContextHolder.getContext();
		if (context != null && context.getIssuer() != null) {
			return context.getIssuer();
		}
		return null;
	}

	public static String getClientId() {
		AuthorizationServerContext context = AuthorizationServerContextHolder.getContext();
		if (context instanceof DefaultAuthorizationServerContext defaultContext) {
			return defaultContext.getClientId();
		}
		return null;
	}

}
