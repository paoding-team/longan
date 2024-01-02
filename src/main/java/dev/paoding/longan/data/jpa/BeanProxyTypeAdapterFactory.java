package dev.paoding.longan.data.jpa;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

public class BeanProxyTypeAdapterFactory implements TypeAdapterFactory {
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        return (BeanProxy.class.isAssignableFrom(type.getRawType()) ? (TypeAdapter<T>) new BeanProxyTypeAdapter(gson) : null);
    }
}
