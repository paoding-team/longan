package dev.paoding.longan.core;


import io.netty.util.AsciiString;

public class Result {
    private AsciiString type;
    private Object value;

    public AsciiString getType() {
        return type;
    }

    public void setType(String type) {
        this.type = AsciiString.cached(type);
    }

    public void setType(AsciiString type) {
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
