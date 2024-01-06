package dev.paoding.longan.service;

import dev.paoding.longan.channel.http.HttpRequestException;
import io.netty.handler.codec.http.HttpResponseStatus;


public class InvalidTokenException extends HttpRequestException {

    public InvalidTokenException(String message) {
        super(message);
        this.code = "unauthorized";
    }


    @Override
    public HttpResponseStatus getHttpResponseStatus() {
        return HttpResponseStatus.UNAUTHORIZED;
    }
}
