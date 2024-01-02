package dev.paoding.longan.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

@Target({METHOD, FIELD, PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Validate {
    String name();

    int validator() default 0;

    String regexp() default "";

    /**
     * The element size must be between the specified boundaries (included).
     * <p>
     * Supported types are:
     * <ul>
     *     <li>{@code CharSequence} (length of character sequence is evaluated)</li>
     *     <li>{@code Collection} (collection size is evaluated)</li>
     *     <li>{@code Map} (map size is evaluated)</li>
     *     <li>Array (array length is evaluated)</li>
     * </ul>
     * <p>
     * {@code null} elements are considered valid.
     */
    long[] size() default {};

    /**
     * The element must not be {@code null}.
     */
    boolean notNull() default false;

    /**
     * The element must not be {@code null} and must contain at least one
     *  non-whitespace character.
     */
    boolean notBlank() default false;

    /**
     * The element must not be {@code null} nor empty. Supported types are:
     * <ul>
     * <li>{@code CharSequence} (length of character sequence is evaluated)</li>
     * <li>{@code Collection} (collection size is evaluated)</li>
     * <li>{@code Map} (map size is evaluated)</li>
     * <li>Array (array length is evaluated)</li>
     * </ul>
     */
    boolean notEmpty() default false;

    /**
     * The id of element must not {@code null}
     */
//    boolean exists() default false;
//
//    boolean isNull() default false;
//
//    boolean isEmail() default false;
//
//    boolean isPhone() default false;

}
