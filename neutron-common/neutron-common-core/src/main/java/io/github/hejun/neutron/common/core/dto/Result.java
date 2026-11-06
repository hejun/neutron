package io.github.hejun.neutron.common.core.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统一返回结果
 *
 * @param <T> 数据类型
 * @author HeJun
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Result<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 成功状态码
     */
    private static final int SUCCESS_CODE = 200;

    /**
     * 成功提示
     */
    private static final String SUCCESS_MESSAGE = "成功";

    private int code;

    private String message;

    private T data;

    /**
     * 成功
     *
     * @param data 返回数据
     * @param <T>  数据类型
     * @return 统一返回结果
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.code = SUCCESS_CODE;
        result.message = SUCCESS_MESSAGE;
        result.data = data;
        return result;
    }

    /**
     * 成功
     *
     * @param <T> 数据类型
     * @return 统一返回结果
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 失败
     *
     * @param code    错误码
     * @param message 错误提示
     * @param <T>     数据类型
     * @return 统一返回结果
     */
    public static <T> Result<T> error(int code, String message) {
        return error(code, message, null);
    }

    /**
     * 失败
     *
     * @param code    错误码
     * @param message 错误提示
     * @param data    返回数据
     * @param <T>     数据类型
     * @return 统一返回结果
     */
    public static <T> Result<T> error(int code, String message, T data) {
        Result<T> result = new Result<>();
        result.code = code;
        result.message = message;
        result.data = data;
        return result;
    }

}
