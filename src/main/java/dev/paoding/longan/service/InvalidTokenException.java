package dev.paoding.longan.service;

import io.netty.handler.codec.http.HttpResponseStatus;


public class InvalidTokenException extends ServiceException {
    private static final long serialVersionUID = 0L;

    public InvalidTokenException(String message) {
        super(message);
        this.code = "unauthorized";
    }


    @Override
    public HttpResponseStatus getHttpResponseStatus() {
        return HttpResponseStatus.UNAUTHORIZED;
    }
}
