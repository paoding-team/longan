package dev.paoding.longan.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

//@Target({METHOD, FIELD, PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Param {
    String name();

    String alias() default "";

    String example() default "";

    String description() default "";

//    String message() default "";

    String regexp() default "";

    int validator() default 0;

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

//    Prop[] props() default {};

//    Valid[] valid() default {};

    /**
     * The element must not be {@code null}.
     * Accepts any type.
     */
    boolean notNull() default false;

    /**
     * The element must not be {@code null} and must contain at least one
     * non-whitespace character. Accepts {@code CharSequence}.
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

//    boolean exists() default false;

//    boolean isNull() default false;

    /**
     * The string has to be a well-formed email address. Exact semantics of what makes up a valid
     * email address are left to Bean Validation providers. Accepts {@code CharSequence}.
     */
//    boolean isEmail() default false;

//    boolean isPhone() default false;
}
