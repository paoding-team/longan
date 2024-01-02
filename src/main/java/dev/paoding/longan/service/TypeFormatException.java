package dev.paoding.longan.service;

import io.netty.handler.codec.http.HttpResponseStatus;


public class TypeFormatException extends ServiceException {
    private static final long serialVersionUID = 0L;

    public TypeFormatException(String message) {
        super(message);
        this.code = "data.type.error";
    }

    @Override
    public HttpResponseStatus getHttpResponseStatus() {
        return HttpResponseStatus.BAD_REQUEST;
    }
}
