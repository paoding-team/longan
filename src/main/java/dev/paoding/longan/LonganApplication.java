package dev.paoding.longan;

import dev.paoding.longan.core.LonganListableBeanFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.util.StopWatch;

public class LonganApplication {

    public static void run(Class<?> primarySource) {
        Logger logger = LoggerFactory.getLogger(primarySource);
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        logger.info("Starting Application using Java {}", System.getProperty("java.version"));
        LonganListableBeanFactory longanListableBeanFactory = new LonganListableBeanFactory(primarySource);
        AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext(longanListableBeanFactory);
        annotationConfigApplicationContext.register(primarySource);
        annotationConfigApplicationContext.refresh();
        stopWatch.stop();
        logger.info("Started Application in {} seconds.", String.format("%.3f", stopWatch.getTotalTimeSeconds()));
    }
}
