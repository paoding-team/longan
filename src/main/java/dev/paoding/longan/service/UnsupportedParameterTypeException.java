package dev.paoding.longan.service;

import dev.paoding.longan.channel.http.HttpRequestException;
import io.netty.handler.codec.http.HttpResponseStatus;

public class UnsupportedParameterTypeException extends HttpRequestException {
    public UnsupportedParameterTypeException(String parameterName) {
        super("The '" + parameterName + "' parameter is of an unsupported type.");
        this.responseType = responseType;
        this.code = "unsupported_parameter_type";
    }

    @Override
    public HttpResponseStatus getHttpResponseStatus() {
        return HttpResponseStatus.BAD_REQUEST;
    }
}
