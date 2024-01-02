package dev.paoding.longan.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Target({FIELD})
@Retention(RUNTIME)
public @interface Json {
    boolean serialize() default true;

//    String[] include() default {};

//    String[] exclude() default {};

//    @AliasFor("value")
//    Filter[] filter() default {};

//    @AliasFor("filter")
//    Filter[] value() default {};

}
