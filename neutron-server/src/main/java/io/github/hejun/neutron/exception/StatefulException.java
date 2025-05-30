package io.github.hejun.neutron.exception;

import lombok.Getter;

/**
 * 有状态异常
 *
 * @author HeJun
 */
@Getter
public class StatefulException extends Exception {

    private final Integer code;

    public StatefulException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public StatefulException(Integer code, Throwable cause) {
        super(cause);
        this.code = code;
    }

    public StatefulException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

}
