package io.github.hejun.neutron.auth.exception;

/**
 * 占用异常
 *
 * @author HeJun
 */
public class OccupiedException extends RuntimeException {

    public OccupiedException(String msg) {
        super(msg);
    }

}
