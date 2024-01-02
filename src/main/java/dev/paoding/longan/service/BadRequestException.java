package dev.paoding.longan.service;

import io.netty.handler.codec.http.HttpResponseStatus;

import java.io.Serial;


public class BadRequestException extends ServiceException {
    @Serial
    private static final long serialVersionUID = 0L;

    public BadRequestException() {
        super("unexpected json data");
        this.code = "unexpected.json.data";
    }

    @Override
    public HttpResponseStatus getHttpResponseStatus() {
        return HttpResponseStatus.BAD_REQUEST;
    }


}
