package io.github.hejun.neutron.auth.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.EnumeratedValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 性别
 *
 * @author HeJun
 */
@Getter
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public enum Gender {

    MALE(1, "男"),
    FEMALE(2, "女");

    @JsonValue
    @EnumeratedValue
    private final int code;
    private final String desc;

    @JsonCreator
    public static Gender fromCode(Integer code) {
        return FEMALE.getCode() == code ? FEMALE : MALE;
    }

}
