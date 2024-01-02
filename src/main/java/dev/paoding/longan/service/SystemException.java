package dev.paoding.longan.service;

import io.netty.handler.codec.http.HttpResponseStatus;


public class SystemException extends ServiceException {
    private static final long serialVersionUID = 0L;


    public SystemException(String message) {
        super(message);
        this.code = "internal.server.error";
    }

    public SystemException(Throwable cause) {
        super(cause);
        this.code = "internal.server.error";
    }

    public SystemException(String message, Throwable cause) {
        super(message, cause);
        this.code = "internal.server.error";
    }

    @Override
    public HttpResponseStatus getHttpResponseStatus() {
        return HttpResponseStatus.INTERNAL_SERVER_ERROR;
    }
}
