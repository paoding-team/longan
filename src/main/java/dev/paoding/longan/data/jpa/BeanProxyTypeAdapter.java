package dev.paoding.longan.data.jpa;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class BeanProxyTypeAdapter extends TypeAdapter<BeanProxy> {
    private final Gson gson;

    public BeanProxyTypeAdapter(Gson gson) {
        this.gson = gson;
    }

    @Override
    public void write(JsonWriter out, BeanProxy value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        Object original = value.getOriginal();
        Class<?> baseType = original.getClass();
        TypeAdapter delegate = gson.getAdapter(TypeToken.get(baseType));
        delegate.write(out, original);
    }

    @Override
    public BeanProxy read(JsonReader in) throws IOException {
        throw new UnsupportedOperationException("Not supported");
    }
}
