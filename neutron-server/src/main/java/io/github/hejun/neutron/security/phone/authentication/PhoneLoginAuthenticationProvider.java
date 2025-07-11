package io.github.hejun.neutron.security.phone.authentication;

import io.github.hejun.neutron.security.sendVerifyCode.constants.VerifyCodeAuthorizationKeyGenerator;
import io.github.hejun.neutron.security.sendVerifyCode.constants.VerifyCodeType;
import io.github.hejun.neutron.security.sendVerifyCode.token.VerifyCodeOAuth2Token;
import io.github.hejun.neutron.service.UserDetailsEnhanceService;
import io.github.hejun.neutron.util.ContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.util.StringUtils;

import java.time.Instant;

/**
 * 手机验证码登录 - 检验验证码 AuthenticationConverter
 *
 * @author HeJun
 */
@Slf4j
public class PhoneLoginAuthenticationProvider implements AuthenticationProvider {

	private final UserDetailsEnhanceService userDetailsEnhanceService;
	private final OAuth2AuthorizationService authorizationService;

	public PhoneLoginAuthenticationProvider(UserDetailsEnhanceService userDetailsEnhanceService,
											OAuth2AuthorizationService authorizationService) {
		this.userDetailsEnhanceService = userDetailsEnhanceService;
		this.authorizationService = authorizationService;
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String issuer = ContextUtils.getIssuer();
		String phone = authentication.getName();
		if (log.isInfoEnabled()) {
			log.info("Issuer: {}, phone: {}", issuer, phone);
		}

		String authorizationId = VerifyCodeAuthorizationKeyGenerator.generate(VerifyCodeType.LOGIN, phone);
		OAuth2Authorization authorization = authorizationService.findById(authorizationId);
		OAuth2Authorization.Token<VerifyCodeOAuth2Token> authorizationToken;
		if (authorization == null || (authorizationToken = authorization.getToken(VerifyCodeOAuth2Token.class)) == null){
			throw new BadCredentialsException("Invalid verify code");
		}
		VerifyCodeOAuth2Token verifyCodeOAuth2Token = authorizationToken.getToken();
		if (verifyCodeOAuth2Token.getInvalidAt().isBefore(Instant.now())){
			throw new BadCredentialsException("Invalid verify code");
		}
		if (!verifyCodeOAuth2Token.getTokenValue().equals(authentication.getCredentials())){
			throw new BadCredentialsException("Invalid verify code");
		}
		UserDetails user;
		if (!StringUtils.hasText(phone) || (user = userDetailsEnhanceService.loadUserByPhone(phone)) == null) {
			throw new BadCredentialsException("Invalid phone number");
		}

		PhoneLoginAuthenticationToken token = new PhoneLoginAuthenticationToken(user, user.getAuthorities());
		token.setDetails(authentication.getDetails());
		return token;
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return PhoneLoginAuthenticationToken.class.isAssignableFrom(authentication);
	}

}
