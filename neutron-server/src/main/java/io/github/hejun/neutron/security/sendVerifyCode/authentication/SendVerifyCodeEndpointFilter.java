package io.github.hejun.neutron.security.sendVerifyCode.authentication;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2ErrorAuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 发送验证码 EndpointFilter
 *
 * @author HeJun
 */
@Setter
public class SendVerifyCodeEndpointFilter extends OncePerRequestFilter {

	private RequestMatcher requestMatcher;
	private AuthenticationManager authenticationManager;
	private AuthenticationConverter authenticationConverter;

	private final AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource = new WebAuthenticationDetailsSource();
	private final AuthenticationSuccessHandler authenticationSuccessHandler = this::sendAuthorizationSuccessResponse;
	private final AuthenticationFailureHandler authenticationFailureHandler = new OAuth2ErrorAuthenticationFailureHandler();
	private final GenericHttpMessageConverter<Object> jsonMessageConverter = new MappingJackson2HttpMessageConverter();

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
									FilterChain chain) throws ServletException, IOException {
		if (!requestMatcher.matches(request)) {
			chain.doFilter(request, response);
			return;
		}
		Authentication authentication = this.authenticationConverter.convert(request);
		if (authentication instanceof SendVerifyCodeAuthenticationToken authenticationToken) {
			authenticationToken.setDetails(this.authenticationDetailsSource.buildDetails(request));
		}
		try {
			Authentication result = authenticationManager.authenticate(authentication);
			this.authenticationSuccessHandler.onAuthenticationSuccess(request, response, result);
		} catch (AuthenticationException ex) {
			this.authenticationFailureHandler.onAuthenticationFailure(request, response, ex);
		}
	}

	private void sendAuthorizationSuccessResponse(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
		Map<String, Object> resp = new HashMap<>();
		if (authentication instanceof SendVerifyCodeAuthenticationToken token) {
			resp.put("expireIn", token.getExpiresIn());
			resp.put("invalidIn", token.getInvalidIn());
		}
		ServletServerHttpResponse httpResponse = new ServletServerHttpResponse(response);
		this.jsonMessageConverter.write(resp, null, httpResponse);
	}

}
