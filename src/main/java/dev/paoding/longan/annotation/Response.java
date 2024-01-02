package dev.paoding.longan.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Target({METHOD})
@Retention(RUNTIME)
public @interface Response {
//    boolean serialize() default true;

//    String[] includes() default {};

//    @AliasFor("value")
    Filter[] filters() default {};

//    @AliasFor("filters")
//    Filter[] value() default {};

    String alias() default "";

    String sample() default "";

    String description() default "";

}
