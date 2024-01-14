package dev.paoding.longan.service;


import dev.paoding.longan.channel.http.HttpRequestException;
import io.netty.handler.codec.http.HttpResponseStatus;


public class AuthorizationException extends HttpRequestException {

    public AuthorizationException(String message) {
        super(message);
        this.code = "FORBIDDEN";
    }

    @Override
    public HttpResponseStatus getHttpResponseStatus() {
        return HttpResponseStatus.FORBIDDEN;
    }


}
