package io.github.hejun.neutron.common.persist.annotation;

import org.hibernate.annotations.IdGeneratorType;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 雪花算法ID注解
 *
 * @author HeJun
 */
@Target({METHOD, FIELD})
@Retention(RUNTIME)
@IdGeneratorType(SnowflakeIdGenerator.class)
public @interface SnowflakeGenerator {
}
