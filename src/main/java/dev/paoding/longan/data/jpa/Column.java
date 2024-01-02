package dev.paoding.longan.data.jpa;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(FIELD)
public @interface Column {
    int length() default 256;

    String alias() default "";

    String example() default "";

    String description() default "";

    boolean nullable() default true;

    boolean insertable() default true;

    boolean updatable() default true;

    boolean unique() default false;

    /**
     * 精度
     *
     * @return 精度
     */
    int scale() default 0;

    /**
     * 长度
     *
     * @return 长度
     */
    int precision() default 0;
}
