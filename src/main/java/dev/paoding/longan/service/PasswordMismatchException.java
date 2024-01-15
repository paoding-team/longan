package dev.paoding.longan.service;

import io.netty.handler.codec.http.HttpResponseStatus;

public class PasswordMismatchException extends ServiceException {
    public PasswordMismatchException() {
        super("Password do not match");
        this.code = "password.mismatch";
    }

    public HttpResponseStatus getHttpResponseStatus() {
        return HttpResponseStatus.UNAUTHORIZED;
    }
}
