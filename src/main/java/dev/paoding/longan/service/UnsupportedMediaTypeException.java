package dev.paoding.longan.service;

import dev.paoding.longan.channel.http.HttpRequestException;
import io.netty.handler.codec.http.HttpResponseStatus;

public class UnsupportedMediaTypeException extends HttpRequestException {
    public UnsupportedMediaTypeException(String responseType) {
        super("Unsupported Media Type");
        this.responseType = responseType;
        this.code = "unsupported_media_type";
    }

    @Override
    public HttpResponseStatus getHttpResponseStatus() {
        return HttpResponseStatus.UNSUPPORTED_MEDIA_TYPE;
    }
}
