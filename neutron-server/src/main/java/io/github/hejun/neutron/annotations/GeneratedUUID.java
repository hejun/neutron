package io.github.hejun.neutron.annotations;

import io.github.hejun.neutron.annotations.impl.GeneratedUUIDImpl;
import org.hibernate.annotations.IdGeneratorType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义主键生成策略
 *
 * @author HeJun
 */
@IdGeneratorType(GeneratedUUIDImpl.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface GeneratedUUID {
}
