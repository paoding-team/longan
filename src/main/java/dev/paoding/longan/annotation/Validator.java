package dev.paoding.longan.annotation;

public @interface Validator {
    Class<?> type();

    int id() default 0;

    Validate[] validates() default {};
}
