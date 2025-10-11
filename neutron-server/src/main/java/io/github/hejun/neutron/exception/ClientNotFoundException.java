package io.github.hejun.neutron.exception;

import org.springframework.security.authentication.AccountStatusException;

/**
 * 客户端不存在异常
 *
 * @author HeJun
 */
public class ClientNotFoundException extends AccountStatusException {

	public ClientNotFoundException(String msg) {
		super(msg);
	}

}
