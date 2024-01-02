package dev.paoding.longan.service;


import io.netty.handler.codec.http.HttpResponseStatus;


public class AuthorizationException extends ServiceException {
    private static final long serialVersionUID = 0L;

    public AuthorizationException(String message) {
        super(message);
        this.code = "";
    }

    @Override
    public HttpResponseStatus getHttpResponseStatus() {
        return HttpResponseStatus.FORBIDDEN;
    }


}
