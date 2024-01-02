package dev.paoding.longan.service;

import io.netty.handler.codec.http.HttpResponseStatus;

public class MethodNotAllowedException extends RuntimeException {
    private String code;
    private String message;
    private String responseType;

    public MethodNotAllowedException(String responseType) {
        this.code = "method_not_allowed";
        this.message = "The method not allowed, allow: GET, POST.";
        this.responseType = responseType;
    }

    public HttpResponseStatus getHttpResponseStatus() {
        return HttpResponseStatus.METHOD_NOT_ALLOWED;
    }
}
