package dev.paoding.longan.channel.http;

import io.netty.buffer.ByteBuf;

public interface HttpResponse {
    String getHeader(String name);

    ByteBuf getContent();
}
