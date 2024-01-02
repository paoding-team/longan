package dev.paoding.longan.annotation;

import dev.paoding.longan.channel.http.Channel;
import dev.paoding.longan.channel.http.RequestMethod;
import dev.paoding.longan.channel.http.ResponseType;
import org.springframework.core.annotation.AliasFor;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Transactional(rollbackFor = Throwable.class)
public @interface Mapping {

    @AliasFor(annotation = Transactional.class)
    boolean readOnly() default true;

    String path() default "";

    RequestMethod method() default RequestMethod.POST;

    Channel[] channel() default {Channel.HTTP};

    String responseType() default ResponseType.APPLICATION_JSON;

    String alias() default "";

    String[] clients() default {"ACCOUNT_ALL"};

    String description() default "";

}
