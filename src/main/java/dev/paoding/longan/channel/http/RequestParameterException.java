package dev.paoding.longan.channel.http;

import io.netty.handler.codec.http.HttpResponseStatus;

public class RequestParameterException extends HttpRequestException {

    public RequestParameterException( String message) {
        super(message);
        this.code = "request.parameter.exception";
    }

    public RequestParameterException(String responseType, String message) {
        super(message);
        this.code = "request.parameter.exception";
        this.responseType = responseType;
    }

    @Override
    public HttpResponseStatus getHttpResponseStatus() {
        return HttpResponseStatus.NOT_ACCEPTABLE;
    }
}
