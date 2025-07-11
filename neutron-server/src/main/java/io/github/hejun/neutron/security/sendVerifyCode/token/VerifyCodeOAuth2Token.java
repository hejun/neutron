package io.github.hejun.neutron.security.sendVerifyCode.token;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.oauth2.core.OAuth2Token;

import java.time.Instant;

/**
 * 短信验证码 OAuth2Token
 *
 * @author HeJun
 */
@AllArgsConstructor
public final class VerifyCodeOAuth2Token implements OAuth2Token {

	private String tokenValue;
	private Instant issuedAt;
	private Instant expiresAt;
	@Getter
	private Instant invalidAt;

	@Override
	public String getTokenValue() {
		return this.tokenValue;
	}

	@Override
	public Instant getIssuedAt() {
		return issuedAt;
	}

	@Override
	public Instant getExpiresAt() {
		return expiresAt;
	}

}
