package dev.paoding.longan.service;

import dev.paoding.longan.core.MethodInvocation;
import io.netty.handler.codec.http.HttpResponseStatus;


public class InternalServerException extends RuntimeException {
    private String code;
    protected MethodInvocation methodInvocation;

    public InternalServerException(String message) {
        super(message);
        this.code = "internal.server.error";
    }

    public InternalServerException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public MethodInvocation getMethodInvocation() {
        return methodInvocation;
    }

    public void setMethodInvocation(MethodInvocation methodInvocation) {
        this.methodInvocation = methodInvocation;
    }

    public HttpResponseStatus getHttpResponseStatus() {
        return HttpResponseStatus.INTERNAL_SERVER_ERROR;
    }
}
