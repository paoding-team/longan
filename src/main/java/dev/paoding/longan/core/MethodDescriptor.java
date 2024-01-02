package dev.paoding.longan.core;

import java.lang.reflect.Method;

public class MethodDescriptor {
    private int LineNumber;
    private Method method;

    public MethodDescriptor(Method method) {
        this.method = method;
    }

    public int getLineNumber() {
        return LineNumber;
    }

    public void setLineNumber(int lineNumber) {
        LineNumber = lineNumber;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }
}
