package dev.paoding.longan.service;

import dev.paoding.longan.channel.http.HttpRequestException;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.io.Serial;


public class UnexpectedJsonException extends HttpRequestException {
    @Serial
    private static final long serialVersionUID = 0L;

    public UnexpectedJsonException() {
        super("unexpected json data");
        this.code = "unexpected.json.data";
    }

    public UnexpectedJsonException(String message) {
        super(message);
        this.code = "unexpected.json.data";
    }

    @Override
    public HttpResponseStatus getHttpResponseStatus() {
        return HttpResponseStatus.BAD_REQUEST;
    }


}
