package dev.paoding.longan.service;

import dev.paoding.longan.core.MethodInvocation;
import io.netty.handler.codec.http.HttpResponseStatus;

public abstract class ServiceException extends RuntimeException {
    protected String code;
    protected MethodInvocation methodInvocation;

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(Throwable cause) {
        super(cause);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getCode() {
        return this.code;
    }

    public MethodInvocation getMethodInvocation() {
        return methodInvocation;
    }

    public void setMethodInvocation(MethodInvocation methodInvocation) {
        this.methodInvocation = methodInvocation;
    }

    public abstract HttpResponseStatus getHttpResponseStatus();

}
