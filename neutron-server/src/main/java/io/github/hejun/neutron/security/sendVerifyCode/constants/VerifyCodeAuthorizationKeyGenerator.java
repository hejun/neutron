package io.github.hejun.neutron.security.sendVerifyCode.constants;

/**
 * 验证码类型
 *
 * @author HeJun
 */
public final class VerifyCodeAuthorizationKeyGenerator {

	public static String generate(VerifyCodeType type, String phone) {
		return "VERIFY_CODE:" + type + ":" + phone;
	}

}
