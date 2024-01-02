package dev.paoding.longan.service;

import dev.paoding.longan.channel.http.HttpRequestException;
import dev.paoding.longan.channel.http.ResponseType;
import io.netty.handler.codec.http.HttpResponseStatus;

public class UnexpectedJsonDataException extends HttpRequestException {

    public UnexpectedJsonDataException() {
        super( "unexpected json data");
        this.code = "unexpected.json.data";
        this.responseType = ResponseType.APPLICATION_JSON;
    }

    public UnexpectedJsonDataException(String parameterName) {
        super( "The '" + parameterName + "' parameter fails to deserialize the JSON object.");
        this.code = "unexpected.json.data";
        this.responseType = ResponseType.APPLICATION_JSON;
    }

    @Override
    public HttpResponseStatus getHttpResponseStatus() {
        return HttpResponseStatus.BAD_REQUEST;
    }
}
