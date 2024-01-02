package dev.paoding.longan.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 注解需要国际化的字段，被注解的成员必须为 String 类型
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface I18n {
}
