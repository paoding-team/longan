package dev.paoding.longan.service;

import io.netty.handler.codec.http.HttpResponseStatus;


public class MethodNotFoundException extends RuntimeException  {
    private static final long serialVersionUID = 0L;

    public MethodNotFoundException(String message) {
        super(message);
    }


    public HttpResponseStatus getHttpResponseStatus() {
        return HttpResponseStatus.NOT_FOUND;
    }

}
