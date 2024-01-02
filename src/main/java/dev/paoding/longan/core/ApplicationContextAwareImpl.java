package dev.paoding.longan.core;

import dev.paoding.longan.data.jpa.DatabaseMetaData;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class ApplicationContextAwareImpl implements ApplicationContextAware {
    @Resource
    private DatabaseMetaData databaseMetaData;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        databaseMetaData.populate();
    }
}
