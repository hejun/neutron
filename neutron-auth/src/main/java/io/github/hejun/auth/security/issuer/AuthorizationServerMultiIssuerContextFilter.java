package io.github.hejun.auth.security.issuer;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jspecify.annotations.NonNull;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContext;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 全局issuer拦截器
 *
 * @author HeJun
 * @see org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.AuthorizationServerContextFilter
 */
public class AuthorizationServerMultiIssuerContextFilter extends OncePerRequestFilter {

	private final BearerTokenResolver bearerTokenResolver = new DefaultBearerTokenResolver();

	private final RequestCache requestCache;
	private final OAuth2AuthorizationService authorizationService;
	private final AuthorizationServerSettings authorizationServerSettings;
	private final IssuerResolver issuerResolver;

	public AuthorizationServerMultiIssuerContextFilter(RequestCache requestCache, OAuth2AuthorizationService authorizationService,
													   AuthorizationServerSettings authorizationServerSettings) {
		this.requestCache = requestCache;
		this.authorizationService = authorizationService;
		this.authorizationServerSettings = authorizationServerSettings;
		this.issuerResolver = new IssuerResolver(authorizationServerSettings);
	}

	@Override
	protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
									@NonNull FilterChain filterChain) throws ServletException, IOException {
		AuthorizationServerContext context = AuthorizationServerContextHolder.getContext();
		if (context != null) {
			filterChain.doFilter(request, response);
			return;
		}

		String issuer = null;

		String accessToken = bearerTokenResolver.resolve(request);
		if (accessToken != null) {
			OAuth2Authorization authorization = authorizationService.findByToken(accessToken, OAuth2TokenType.ACCESS_TOKEN);
			if (authorization != null) {
				OAuth2AuthorizationRequest authorizationRequest = authorization.getAttribute(OAuth2AuthorizationRequest.class.getName());
				issuer = this.issuerResolver.resolve(authorizationRequest);
			}
		}

		if (!StringUtils.hasText(issuer)) {
			SavedRequest savedRequest = requestCache.getRequest(request, response);
			if (savedRequest != null) {
				issuer = this.issuerResolver.resolve(savedRequest);
			}
		}

		// 针对进入 /consent 页面, RequestCache会专门移除之前缓存的信息单独处理
		if (!StringUtils.hasText(issuer)) {
			String state = request.getParameter(OAuth2ParameterNames.STATE);
			if (StringUtils.hasText(state)) {
				OAuth2Authorization authorization = authorizationService.findByToken(state, null);
				if (authorization != null) {
					OAuth2AuthorizationRequest authorizationRequest = authorization.getAttribute(OAuth2AuthorizationRequest.class.getName());
					issuer = this.issuerResolver.resolve(authorizationRequest);
				}
			}
		}

		if (StringUtils.hasText(issuer)) {
			try {
				AuthorizationServerContext authorizationServerContext = new DefaultAuthorizationServerContext(issuer,
					this.authorizationServerSettings);
				AuthorizationServerContextHolder.setContext(authorizationServerContext);
				filterChain.doFilter(request, response);
			} finally {
				AuthorizationServerContextHolder.resetContext();
			}
		} else {
			filterChain.doFilter(request, response);
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

		private String resolve(OAuth2AuthorizationRequest oAuth2AuthorizationRequest) {
			if (this.issuer != null) {
				return this.issuer;
			}
			String authorizationUri = oAuth2AuthorizationRequest.getAuthorizationUri();
			String path = UriComponentsBuilder
				.fromUriString(authorizationUri)
				.scheme(null)
				.host(null)
				.port(null)
				.replaceQuery(null)
				.toUriString();
			return this.resolve(path, authorizationUri);
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

	@Getter
	@AllArgsConstructor
	private static final class DefaultAuthorizationServerContext implements AuthorizationServerContext {

		private final String issuer;

		private final AuthorizationServerSettings authorizationServerSettings;

	}

}
