package dev.paoding.longan.channel.http;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpRequest;

import java.util.List;

public class HttpRequestImpl implements HttpRequest {
    private FullHttpRequest request;

    public HttpRequestImpl(FullHttpRequest request) {
        this.request = request;
    }

    @Override
    public String getHeader(String name) {
        return request.headers().get(name);
    }

    @Override
    public String getHeader(CharSequence name) {
        return request.headers().get(name);
    }

    @Override
    public String getHeader(CharSequence name, String defaultValue) {
        return request.headers().get(name, defaultValue);
    }

    @Override
    public List<String> getAll(String name) {
        return request.headers().getAll(name);
    }

    @Override
    public List<String> getAll(CharSequence name) {
        return request.headers().getAll(name);
    }

    @Override
    public String getMethod() {
        return request.method().name();
    }

    @Override
    public String getPathInfo() {
        return request.uri();
    }

    @Override
    public ByteBuf getContent() {
        return request.content();
    }
}
