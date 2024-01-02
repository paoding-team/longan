package dev.paoding.longan.annotation;

import java.lang.annotation.*;
import java.lang.invoke.SerializedLambda;

@Target({ElementType.ANNOTATION_TYPE,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Filter {
    Class<?> type();

    String[] includes() default {};

//    String[] exclude() default {};
}
