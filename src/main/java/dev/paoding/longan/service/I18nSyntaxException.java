package dev.paoding.longan.service;

import io.netty.handler.codec.http.HttpResponseStatus;


public class I18nSyntaxException extends ServiceException {
    private static final long serialVersionUID = 0L;

    public I18nSyntaxException(String json) {
        super("unexpected i18n content: " + json);
        this.code = "internal_server_error";
    }

    @Override
    public HttpResponseStatus getHttpResponseStatus() {
        return HttpResponseStatus.INTERNAL_SERVER_ERROR;
    }
}
