package dev.paoding.longan.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Var {
    String name();

    String alias();

    String sample();
}
