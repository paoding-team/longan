package dev.paoding.longan.annotation;

import dev.paoding.longan.LonganAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(LonganAutoConfiguration.class)
@ComponentScan
public @interface LonganBootApplication {
}
