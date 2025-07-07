package io.github.hejun.neutron.util;

import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContext;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;

/**
 * ContextUtil
 *
 * @author HeJun
 */
public class ContextUtils {

	public static String getIssuer() {
		AuthorizationServerContext context = AuthorizationServerContextHolder.getContext();
		if (context != null && context.getIssuer() != null) {
			return context.getIssuer();
		}
		return null;
	}

}
