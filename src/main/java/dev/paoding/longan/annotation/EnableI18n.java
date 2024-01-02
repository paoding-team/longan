package dev.paoding.longan.annotation;

import dev.paoding.longan.core.Internationalization;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用国际化支持，需要客户端在http header中传递Content-Language到服务端。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(Internationalization.class)
public @interface EnableI18n {

    /**
     * 设置默认地区语言，如果客户端没有传 Content-Language，将采用设定的默认值。
     *
     * @return 默认地区语言
     */
    String value() default "zh_CN";

    /**
     * 仅支持的地区语言，如果客户端传 Content-Language 不在supports范围内，将采用设定的默认值将。
     *
     * @return 支持的地区语言
     */
    String[] supports() default {"en_US", "zh_CN"};
}
