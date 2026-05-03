package io.github.hejun.auth.exception;

import org.springframework.security.authentication.AccountStatusException;

/**
 * 占用异常
 *
 * @author HeJun
 */
public class OccupiedException extends AccountStatusException {

	public OccupiedException(String msg) {
		super(msg);
	}

}
