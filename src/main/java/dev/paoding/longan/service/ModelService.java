package dev.paoding.longan.service;


import dev.paoding.longan.data.jpa.BeanFactory;

public abstract class ModelService<T> {

    public <K> K attach(K bean) {
        return BeanFactory.attach(bean);
    }

}
