package dev.paoding.longan.service;

import dev.paoding.longan.channel.http.HttpRequestException;
import io.netty.handler.codec.http.HttpResponseStatus;

public class DuplicateParameterException extends HttpRequestException {

    public DuplicateParameterException(String responseType, String parameterName) {
        super("The parameter '" + parameterName + "' is not unique");
        this.code = "duplicate_parameter";
        this.responseType = responseType;
    }

    @Override
    public HttpResponseStatus getHttpResponseStatus() {
        return HttpResponseStatus.BAD_REQUEST;
    }
}
