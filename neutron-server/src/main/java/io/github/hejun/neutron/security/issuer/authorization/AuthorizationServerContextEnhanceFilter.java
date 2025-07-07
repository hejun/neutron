package io.github.hejun.neutron.security.issuer.authorization;

import io.github.hejun.neutron.security.issuer.context.DefaultAuthorizationServerContext;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContext;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.*;

/**
 * AuthorizationServerContextFilter 增强处理
 *
 * @author HeJun
 */
public class AuthorizationServerContextEnhanceFilter extends OncePerRequestFilter {

	private final AuthorizationServerSettings authorizationServerSettings;
	private final IssuerResolver issuerResolver;

	private RequestCache requestCache;

	public AuthorizationServerContextEnhanceFilter(AuthorizationServerSettings authorizationServerSettings) {
		Assert.notNull(authorizationServerSettings, "authorizationServerSettings cannot be null");
		this.authorizationServerSettings = authorizationServerSettings;
		this.issuerResolver = new IssuerResolver(authorizationServerSettings);
	}

	public AuthorizationServerContextEnhanceFilter(AuthorizationServerSettings authorizationServerSettings, RequestCache requestCache) {
		Assert.notNull(authorizationServerSettings, "authorizationServerSettings cannot be null");
		this.authorizationServerSettings = authorizationServerSettings;
		this.issuerResolver = new IssuerResolver(authorizationServerSettings);
		this.requestCache = requestCache;
	}

	@Override
	protected void doFilterInternal(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response,
									@Nonnull FilterChain chain) throws ServletException, IOException {
		// AuthorizationServer 环境沿用 AuthorizationServerContextFilter
		if (requestCache == null) {
			try {
				String issuer = this.issuerResolver.resolve(request);
				String clientId = request.getParameter(OAuth2ParameterNames.CLIENT_ID);
				AuthorizationServerContext authorizationServerContext = new DefaultAuthorizationServerContext(issuer, clientId,
					this.authorizationServerSettings);
				AuthorizationServerContextHolder.setContext(authorizationServerContext);
				chain.doFilter(request, response);
			} finally {
				AuthorizationServerContextHolder.resetContext();
			}
		} else {
			// 非 AuthorizationServer 环境
			SavedRequest savedRequest = requestCache.getRequest(request, response);
			if (savedRequest != null) {
				try {
					String issuer = this.issuerResolver.resolve(savedRequest);
					String[] parameterValues = savedRequest.getParameterValues(OAuth2ParameterNames.CLIENT_ID);
					String clientId = Optional.ofNullable(parameterValues).map(arr -> arr[0]).orElse(null);
					AuthorizationServerContext authorizationServerContext = new DefaultAuthorizationServerContext(issuer, clientId,
						this.authorizationServerSettings);
					AuthorizationServerContextHolder.setContext(authorizationServerContext);
					chain.doFilter(request, response);
				} finally {
					AuthorizationServerContextHolder.resetContext();
				}
			} else {
				chain.doFilter(request, response);
			}
		}
	}

	private static final class IssuerResolver {

		private final String issuer;

		private final Set<String> endpointUris;

		private IssuerResolver(AuthorizationServerSettings authorizationServerSettings) {
			if (authorizationServerSettings.getIssuer() != null) {
				this.issuer = authorizationServerSettings.getIssuer();
				this.endpointUris = Collections.emptySet();
			} else {
				this.issuer = null;
				this.endpointUris = new HashSet<>();
				this.endpointUris.add("/.well-known/oauth-authorization-server");
				this.endpointUris.add("/.well-known/openid-configuration");
				for (Map.Entry<String, Object> setting : authorizationServerSettings.getSettings().entrySet()) {
					if (setting.getKey().endsWith("-endpoint")) {
						this.endpointUris.add((String) setting.getValue());
					}
				}
			}
		}

		private String resolve(HttpServletRequest request) {
			if (this.issuer != null) {
				return this.issuer;
			}
			return this.resolve(request.getRequestURI(), UrlUtils.buildFullRequestUrl(request));
		}

		private String resolve(SavedRequest savedRequest) {
			if (this.issuer != null) {
				return this.issuer;
			}
			String redirectUrl = savedRequest.getRedirectUrl();
			String path = UriComponentsBuilder
				.fromUriString(redirectUrl)
				.scheme(null)
				.host(null)
				.port(null)
				.replaceQuery(null)
				.toUriString();
			return this.resolve(path, redirectUrl);
		}

		private String resolve(String path, String fullRequestUrl) {
			if (!StringUtils.hasText(path)) {
				path = "";
			} else {
				for (String endpointUri : this.endpointUris) {
					if (path.contains(endpointUri)) {
						path = path.replace(endpointUri, "");
						break;
					}
				}
			}

			return UriComponentsBuilder.fromUriString(fullRequestUrl)
				.replacePath(path)
				.replaceQuery(null)
				.fragment(null)
				.build()
				.toUriString();
		}

	}

}
