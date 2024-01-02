package dev.paoding.longan.data.jpa;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({})
@Retention(RUNTIME)
public @interface Index {
    String name() default "";
    /**
     * (Optional) The name of the index; defaults to a provider-generated name.
     */

    /**
     * (Required) The names of the columns to be included in the index,
     * in order.
     */
    String[] columnNames() default {};

    /**
     * (Optional) Whether the index is unique.
     */
    boolean unique() default false;
}
