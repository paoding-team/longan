package dev.paoding.longan.channel.http;

import io.netty.buffer.ByteBuf;

import java.util.List;

public interface HttpRequest {
    String getHeader(String name);

    String getHeader(CharSequence name);

    String getHeader(CharSequence name,String defaultValue);

    List<String> getAll(String name);

    List<String> getAll(CharSequence name);

    String getMethod();

    String getPathInfo();

    ByteBuf getContent();
}
