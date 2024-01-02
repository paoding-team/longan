package dev.paoding.longan;

import dev.paoding.longan.core.LonganConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

//@AutoConfiguration
@Configuration
@ComponentScan
@Import({LonganConfiguration.class})
public class LonganAutoConfiguration {


}
