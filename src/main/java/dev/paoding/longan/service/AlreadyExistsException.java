package dev.paoding.longan.service;

import io.netty.handler.codec.http.HttpResponseStatus;

public class AlreadyExistsException extends ServiceException {

    public AlreadyExistsException(String filed) {
        super(filed + " already exists");
        this.code = filed + ".exists";
    }

    public AlreadyExistsException(Class<?> type, String filed) {
        super(type.getSimpleName().toLowerCase() + "." + filed + " already exists");
        this.code = type.getSimpleName().toLowerCase() + "." + filed + ".exists";
    }

    @Override
    public HttpResponseStatus getHttpResponseStatus() {
        return HttpResponseStatus.NOT_ACCEPTABLE;
    }


}
