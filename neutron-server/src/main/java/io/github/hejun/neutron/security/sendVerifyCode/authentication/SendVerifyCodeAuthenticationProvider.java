package io.github.hejun.neutron.security.sendVerifyCode.authentication;

import io.github.hejun.neutron.security.sendVerifyCode.constants.VerifyCodeAuthorizationKeyGenerator;
import io.github.hejun.neutron.security.sendVerifyCode.constants.VerifyCodeType;
import io.github.hejun.neutron.security.sendVerifyCode.token.VerifyCodeOAuth2Token;
import io.github.hejun.neutron.service.SMSService;
import io.github.hejun.neutron.util.ContextUtils;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * 发送验证码 AuthenticationProvider
 *
 * @author HeJun
 */
@Slf4j
@Setter
public class SendVerifyCodeAuthenticationProvider implements AuthenticationProvider {

	private final OAuth2AuthorizationService authorizationService;
	private final RegisteredClientRepository registeredClientRepository;
	private final SMSService smsService;
	private long expireIn = 60;
	private long invalidIn = 300;

	public SendVerifyCodeAuthenticationProvider(OAuth2AuthorizationService authorizationService,
												RegisteredClientRepository registeredClientRepository,
												SMSService smsService) {
		this.authorizationService = authorizationService;
		this.registeredClientRepository = registeredClientRepository;
		this.smsService = smsService;
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		SendVerifyCodeAuthenticationToken authenticationToken = (SendVerifyCodeAuthenticationToken) authentication;

		VerifyCodeType type = authenticationToken.getType();
		String phone = authenticationToken.getName();

		if (type == null || !StringUtils.hasText(phone)) {
			throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST, "Invalid type or phone", null));
		}

		String authorizationId = VerifyCodeAuthorizationKeyGenerator.generate(type, phone);
		this.checkSendAccess(authorizationId);

		RegisteredClient registeredClient = registeredClientRepository.findByClientId(ContextUtils.getClientId());
		if (registeredClient == null) {
			throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST, "Client not found", null));
		}

		String verifyCode = smsService.sendVerifyCode(type, phone);
		Instant issuedAt = Instant.now();
		Instant expireAt = issuedAt.plus(expireIn, ChronoUnit.SECONDS);
		Instant invalidAt = issuedAt.plus(invalidIn, ChronoUnit.SECONDS);

		VerifyCodeOAuth2Token verifyCodeOAuth2Token = new VerifyCodeOAuth2Token(verifyCode, issuedAt, expireAt, invalidAt);

		OAuth2Authorization authorization = OAuth2Authorization
			.withRegisteredClient(registeredClient)
			.id(authorizationId)
			.principalName(phone)
			.authorizationGrantType(new AuthorizationGrantType(OidcScopes.PHONE))
			.token(verifyCodeOAuth2Token)
			.build();
		authorizationService.save(authorization);

		SendVerifyCodeAuthenticationToken result = new SendVerifyCodeAuthenticationToken(phone, type, expireIn, invalidIn);
		result.setDetails(authenticationToken.getDetails());
		return result;
	}

	private void checkSendAccess(String authorizationId) throws OAuth2AuthenticationException {
		OAuth2Authorization check = authorizationService.findById(authorizationId);
		if (check != null) {
			OAuth2Authorization.Token<VerifyCodeOAuth2Token> checkToken = check.getToken(VerifyCodeOAuth2Token.class);
			// need enhance check
			if (checkToken == null || !checkToken.isExpired()) {
				throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.ACCESS_DENIED, "Frequent sending", null));
			}
		}
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return SendVerifyCodeAuthenticationToken.class.isAssignableFrom(authentication);
	}

}
