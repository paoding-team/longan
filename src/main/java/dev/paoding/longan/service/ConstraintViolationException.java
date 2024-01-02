package dev.paoding.longan.service;

import dev.paoding.longan.channel.http.HttpRequestException;
import io.netty.handler.codec.http.HttpResponseStatus;


public class ConstraintViolationException extends HttpRequestException {

    public ConstraintViolationException(String code, String message) {
        super(message);
        this.code = code;
    }

    @Override
    public HttpResponseStatus getHttpResponseStatus() {
        return HttpResponseStatus.BAD_REQUEST;
    }


}
