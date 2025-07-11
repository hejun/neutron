package io.github.hejun.neutron.service.impl;

import io.github.hejun.neutron.security.sendVerifyCode.constants.VerifyCodeType;
import io.github.hejun.neutron.service.SMSService;
import io.github.hejun.neutron.util.ContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
import org.springframework.stereotype.Service;

/**
 * 短信Service
 *
 * @author HeJun
 */
@Slf4j
@Service
public class SMSServiceImpl implements SMSService {

	@Override
	public String sendVerifyCode(VerifyCodeType type, String phone) throws LockedException, AuthenticationServiceException {
		String issuer = ContextUtils.getIssuer();
		String verifyCode = "1234";
		if (log.isInfoEnabled()) {
			log.info("Issuer: {}, type: {}, phone: {}, verifyCode: {}", issuer, type, phone, verifyCode);
		}
		return verifyCode;
	}

}
