package dev.paoding.longan.data.jpa;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target({FIELD})
@Documented
public @interface ManyToMany {

    String role() default "";

    String alias() default "";

    String description() default "";
}
