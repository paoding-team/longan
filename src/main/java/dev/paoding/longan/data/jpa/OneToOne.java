package dev.paoding.longan.data.jpa;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD})
@Retention(RUNTIME)
public @interface OneToOne {

    boolean orphanRemoval() default false;

    String alias() default "";

    String description() default "";
}
