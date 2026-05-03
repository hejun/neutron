package io.github.hejun.gateway.config;

import io.github.hejun.neutron.common.core.dto.Result;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.server.DefaultServerOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.server.DefaultServerRedirectStrategy;
import org.springframework.security.web.server.DelegatingServerAuthenticationEntryPoint;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.security.web.server.savedrequest.ServerRequestCache;
import org.springframework.security.web.server.savedrequest.WebSessionServerRequestCache;
import org.springframework.security.web.server.util.matcher.*;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 安全配置
 *
 * @author HeJun
 */
@Configuration(proxyBeanMethods = false)
public class SecurityConfig {

	@Bean
	public SecurityWebFilterChain webServerSecurityFilterChain(ServerHttpSecurity http, Iterable<ClientRegistration> registrations,
															   ReactiveClientRegistrationRepository clientRegistrationRepository) {
		ServerOAuth2AuthorizationRequestResolver authorizationRequestResolver
			= this.createDynamicRedirectOauth2AuthorizationRequestResolver(clientRegistrationRepository);

		http
			.authorizeExchange((exchange) ->
				exchange
					.pathMatchers("continue", "/actuator/**").permitAll()
					.anyExchange().authenticated()
			)
			// 禁用 Csrf, 先不考虑跨站请求伪造问题
			.csrf(ServerHttpSecurity.CsrfSpec::disable)
			.oauth2Login(oauth2Login ->
				oauth2Login
					.authorizationRequestResolver(authorizationRequestResolver)
			)
			.logout(logout ->
				logout
					.requiresLogout(ServerWebExchangeMatchers.pathMatchers(HttpMethod.GET, "/logout"))
					.logoutSuccessHandler(
						this.createOidcLogoutSuccessHandler(clientRegistrationRepository)
					)
			)
			.oauth2Client(Customizer.withDefaults())
			.exceptionHandling(exceptionHandling ->
				exceptionHandling
					.authenticationEntryPoint(
						this.createDynamicServerAuthenticationEntryPoint(registrations)
					)
			);
		return http.build();
	}

	/**
	 * 根据 <strong>Content-Type</strong> 动态判断返回响应
	 * <p>
	 * application/json 返回 403 并添加可供登录的地址
	 * <p>
	 * 其他返回登录页
	 *
	 * @param registrations 注册的客户端
	 * @return ServerAuthenticationEntryPoint
	 * @see ServerHttpSecurity.OAuth2LoginSpec
	 */
	private ServerAuthenticationEntryPoint createDynamicServerAuthenticationEntryPoint(Iterable<ClientRegistration> registrations) {
		String location = "/login";
		List<DelegatingServerAuthenticationEntryPoint.DelegateEntry> entryPoints = new ArrayList<>();
		ServerRequestCache requestCache = new WebSessionServerRequestCache();
		JsonMapper jsonMapper = JsonMapper.builder().build();

		List<Map<String, String>> loginProviders = this.getLinks(registrations);
		if (!loginProviders.isEmpty()) {

			if (loginProviders.size() == 1) {
				location = loginProviders.getFirst().get("location");
			}

			MediaTypeServerWebExchangeMatcher jsonMediaMatcher = new MediaTypeServerWebExchangeMatcher(MediaType.APPLICATION_JSON);
			jsonMediaMatcher.setIgnoredMediaTypes(Collections.singleton(MediaType.ALL));
			ServerWebExchangeMatcher xhrMatcher = (exchange) -> {
				HttpHeaders headers = exchange.getRequest().getHeaders();
				if (headers.getOrEmpty("X-Requested-With").contains("XMLHttpRequest")
					|| headers.getAccept().contains(MediaType.APPLICATION_JSON)) {
					return ServerWebExchangeMatcher.MatchResult.match();
				}
				return ServerWebExchangeMatcher.MatchResult.notMatch();
			};

			ServerWebExchangeMatcher jsonMatcher = new OrServerWebExchangeMatcher(jsonMediaMatcher, xhrMatcher);
			ServerAuthenticationEntryPoint jsonServerAuthenticationEntryPoint = (exchange, ex) ->
				requestCache
					.saveRequest(exchange)
					.then(Mono.defer(() -> {
						ServerHttpResponse response = exchange.getResponse();
						response.setStatusCode(HttpStatus.OK);
						response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

						byte[] resp = jsonMapper.writeValueAsBytes(Result.ERROR(
							HttpStatus.UNAUTHORIZED.value(),
							HttpStatus.UNAUTHORIZED.getReasonPhrase(),
							Map.of("providers", loginProviders)
						));
						DataBuffer buffer = response.bufferFactory().wrap(resp);
						return response.writeWith(Mono.just(buffer)).doOnError((error) -> DataBufferUtils.release(buffer));
					}));

			entryPoints.add(new DelegatingServerAuthenticationEntryPoint.DelegateEntry(jsonMatcher, jsonServerAuthenticationEntryPoint));

		}

		DelegatingServerAuthenticationEntryPoint entryPoint = new DelegatingServerAuthenticationEntryPoint(entryPoints);
		RedirectServerAuthenticationEntryPoint defaultEntryPoint = new RedirectServerAuthenticationEntryPoint(location);
		defaultEntryPoint.setRequestCache(requestCache);
		entryPoint.setDefaultEntryPoint(defaultEntryPoint);

		return entryPoint;
	}

	private List<Map<String, String>> getLinks(Iterable<ClientRegistration> registrations) {
		if (registrations == null) {
			return Collections.emptyList();
		}

		List<Map<String, String>> result = new ArrayList<>();
		registrations.iterator().forEachRemaining((r) -> {
			if (AuthorizationGrantType.AUTHORIZATION_CODE.equals(r.getAuthorizationGrantType())) {
				result.add(Map.of(
					"clientId", r.getClientId(),
					"clientName", r.getClientName(),
					"location", "/oauth2/authorization/" + r.getRegistrationId()
				));
			}
		});
		return result;
	}

	/**
	 * 在跳转到 /oauth2/authorization/{registrationId} 时
	 * <p>
	 * 如果参数存在 <strong>continue</strong>, 则替换原有路径为 <strong>continue</strong> 指向的路径
	 *
	 * @param clientRegistrationRepository 客户端
	 * @return RequestResolver
	 */
	private ServerOAuth2AuthorizationRequestResolver createDynamicRedirectOauth2AuthorizationRequestResolver(ReactiveClientRegistrationRepository clientRegistrationRepository) {
		// 需要与 WebSessionServerRequestCache 里的 sessionAttrName 一致
		final String sessionAttrName = "SPRING_SECURITY_SAVED_REQUEST";
		final String matchParamKey = "continue";

		ServerRequestCache requestCache = new WebSessionServerRequestCache() {

			@NonNull
			@Override
			public Mono<Void> saveRequest(@NonNull ServerWebExchange exchange) {
				return super.saveRequest(exchange)
					.then(Mono.defer(() -> {
						if (exchange.getRequest().getQueryParams().containsKey(matchParamKey)) {
							String continueUrl = exchange.getRequest().getQueryParams().getFirst(matchParamKey);
							if (StringUtils.hasText(continueUrl)) {
								String decoderContinueUrl = URLDecoder.decode(continueUrl, StandardCharsets.UTF_8);
								return exchange.getSession()
									.map(WebSession::getAttributes)
									.doOnNext(attr -> attr.put(sessionAttrName, decoderContinueUrl))
									.then();
							}
						}
						return Mono.empty();
					}));
			}
		};

		ServerWebExchangeMatcher oauth2AuthorizationRequest
			= new PathPatternParserServerWebExchangeMatcher("/oauth2/authorization/{registrationId}");

		return new DefaultServerOAuth2AuthorizationRequestResolver(clientRegistrationRepository) {
			@Override
			public Mono<OAuth2AuthorizationRequest> resolve(ServerWebExchange exchange, String clientRegistrationId) {
				return oauth2AuthorizationRequest
					.matches(exchange)
					.filter(ServerWebExchangeMatcher.MatchResult::isMatch)
					.filter((matchResult) ->
						exchange
							.getRequest()
							.getQueryParams()
							.containsKey(matchParamKey)
					)
					.flatMap(matchResult ->
						requestCache
							.saveRequest(exchange)
							.then(super.resolve(exchange, clientRegistrationId)))
					.switchIfEmpty(super.resolve(exchange, clientRegistrationId));
			}
		};
	}

	/**
	 * Oidc登出
	 * <p>
	 * 如果参数存在 <strong>continue</strong>, 则将 <strong>continue</strong> 编码为 <strong>state</strong> 参数, auth-server会回传回来, 然后通过这个回传路径重新跳转到前端
	 *
	 * @param clientRegistrationRepository 客户端
	 * @return RequestResolver
	 */
	private ServerLogoutSuccessHandler createOidcLogoutSuccessHandler(ReactiveClientRegistrationRepository clientRegistrationRepository) {
		final String matchParamKey = "continue";

		DefaultServerRedirectStrategy serverRedirectStrategy = new DefaultServerRedirectStrategy() {

			@NonNull
			@Override
			public Mono<Void> sendRedirect(ServerWebExchange exchange, @NonNull URI location) {
				if (exchange.getRequest().getQueryParams().containsKey(matchParamKey)) {
					String continueUrl = exchange.getRequest().getQueryParams().getFirst(matchParamKey);
					if (StringUtils.hasText(continueUrl)) {
						location = UriComponentsBuilder.fromUri(location)
							.queryParam(OAuth2ParameterNames.STATE, continueUrl)
							.build()
							.toUri();
					}
				}
				return super.sendRedirect(exchange, location);
			}
		};

		OidcClientInitiatedServerLogoutSuccessHandler oidcLogoutSuccessHandler =
			new OidcClientInitiatedServerLogoutSuccessHandler(clientRegistrationRepository);
		oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}/continue");
		oidcLogoutSuccessHandler.setRedirectStrategy(serverRedirectStrategy);

		return oidcLogoutSuccessHandler;
	}

	/**
	 * 定义一个 <strong>/continue</strong> 的路由, 接收 <strong>state</strong> 参数
	 * <p>
	 * 用于处理 auth-server 登出回调
	 *
	 * @return RouterFunction
	 */
	@Bean
	public RouterFunction<@NonNull ServerResponse> oidcLogoutRedirectRouter() {
		return RouterFunctions.route()
			.GET("/continue", request -> {
					URI uri = request.queryParam(OAuth2ParameterNames.STATE).map(URI::create).orElseGet(() -> URI.create("/"));
					return ServerResponse
						.temporaryRedirect(uri)
						.build();
				}

			)
			.build();
	}

}
