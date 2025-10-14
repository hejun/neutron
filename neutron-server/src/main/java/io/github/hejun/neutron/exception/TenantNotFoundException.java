package io.github.hejun.neutron.exception;

import org.springframework.security.authentication.AccountStatusException;

/**
 * 租户不存在异常
 *
 * @author HeJun
 */
public class TenantNotFoundException extends AccountStatusException {

	public TenantNotFoundException(String msg) {
		super(msg);
	}

}
