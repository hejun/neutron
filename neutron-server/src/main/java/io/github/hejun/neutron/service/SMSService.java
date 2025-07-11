package io.github.hejun.neutron.service;

import io.github.hejun.neutron.security.sendVerifyCode.constants.VerifyCodeType;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.LockedException;

/**
 * 短信 Service
 *
 * @author HeJun
 */
public interface SMSService {

	String sendVerifyCode(VerifyCodeType type, String phone) throws LockedException, AuthenticationServiceException;

}
