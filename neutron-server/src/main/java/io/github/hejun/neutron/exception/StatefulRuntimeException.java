package io.github.hejun.neutron.exception;

import lombok.Getter;

/**
 * 有状态Runtime异常
 *
 * @author HeJun
 */
@Getter
public class StatefulRuntimeException extends RuntimeException {

	private final Integer code;

	public StatefulRuntimeException(Integer code, String message) {
		super(message);
		this.code = code;
	}

	public StatefulRuntimeException(Integer code, Throwable cause) {
		super(cause);
		this.code = code;
	}

	public StatefulRuntimeException(Integer code, String message, Throwable cause) {
		super(message, cause);
		this.code = code;
	}

}
