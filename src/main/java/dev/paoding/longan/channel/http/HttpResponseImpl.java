package dev.paoding.longan.channel.http;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpResponse;

public class HttpResponseImpl implements HttpResponse {
    private FullHttpResponse response;

    public HttpResponseImpl(FullHttpResponse response) {
        this.response = response;
    }

    @Override
    public String getHeader(String name) {
        return response.headers().get(name);
    }

    @Override
    public ByteBuf getContent() {
        return response.content();
    }
}
