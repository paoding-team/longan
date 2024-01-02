package dev.paoding.longan.channel.http;

import dev.paoding.longan.util.GsonUtils;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.AsciiString;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static dev.paoding.longan.channel.http.Http.ContentType.IMAGE_PNG;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_DISPOSITION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.*;

public abstract class AbstractServiceHandler {

    public abstract FullHttpResponse channelRead(ChannelHandlerContext ctx, FullHttpRequest request);

    protected FullHttpResponse writeJson(HttpVersion httpVersion, HttpResponseStatus httpResponseStatus, ExceptionResult exceptionResult) {
        String content = GsonUtils.toJson(exceptionResult);
        return writeJson(httpVersion, httpResponseStatus, content);
    }

    protected FullHttpResponse writeJson(HttpVersion httpVersion, HttpResponseStatus httpResponseStatus, String json) {
        return write(httpVersion, httpResponseStatus, json.getBytes(StandardCharsets.UTF_8), APPLICATION_JSON);
    }

    protected FullHttpResponse writeText(HttpVersion httpVersion, HttpResponseStatus httpResponseStatus, String text) {
        return write(httpVersion, httpResponseStatus, text.getBytes(StandardCharsets.UTF_8), TEXT_PLAIN);
    }

    protected FullHttpResponse writeXml(HttpVersion httpVersion, HttpResponseStatus status, String text) {
        return write(httpVersion, status, text.getBytes(StandardCharsets.UTF_8), APPLICATION_XML);
    }

    protected FullHttpResponse writeHtml(HttpVersion httpVersion, HttpResponseStatus status, String text) {
        return write(httpVersion, status, text.getBytes(StandardCharsets.UTF_8), TEXT_HTML);
    }

    protected FullHttpResponse writeNoContent(HttpVersion httpVersion) {
        FullHttpResponse response = new DefaultFullHttpResponse(httpVersion, HttpResponseStatus.NO_CONTENT, Unpooled.wrappedBuffer(new byte[]{}));
        HttpUtil.setContentLength(response, 0);
        postHandle(new HttpResponseImpl(response));
        return response;
    }

    protected FullHttpResponse write(HttpVersion httpVersion, HttpResponseStatus status, String text, AsciiString contentType) {
        return write(httpVersion, status, text.getBytes(StandardCharsets.UTF_8), contentType);
    }

    protected FullHttpResponse write(HttpVersion httpVersion, HttpResponseStatus httpResponseStatus, byte[] bytes, AsciiString contentType) {
        FullHttpResponse response = new DefaultFullHttpResponse(httpVersion,
                httpResponseStatus, Unpooled.wrappedBuffer(bytes));
        response.headers().set(CONTENT_TYPE, contentType);
        HttpUtil.setContentLength(response, bytes.length);
        postHandle(new HttpResponseImpl(response));
        return response;
    }


    protected FullHttpResponse write(HttpVersion httpVersion, ByteFile byteFile) throws IOException {
        FullHttpResponse response = new DefaultFullHttpResponse(httpVersion,
                HttpResponseStatus.OK, Unpooled.wrappedBuffer(byteFile.getContent()));
        response.headers().set(CONTENT_TYPE, APPLICATION_OCTET_STREAM);
        String filename = URLEncoder.encode(byteFile.getName(), "UTF-8");
        response.headers().set(CONTENT_DISPOSITION, "attachment;filename*=UTF-8''" + filename);
        HttpUtil.setContentLength(response, byteFile.length());
        postHandle(new HttpResponseImpl(response));
        return response;
    }

    protected static AsciiString getContentType(String uri) {
        AsciiString contentType;
        if (uri.endsWith("html") || uri.endsWith("htm")) {
            contentType = TEXT_HTML;
        } else if (uri.endsWith("js")) {
            contentType = Http.ContentType.APPLICATION_JAVASCRIPT;
        } else if (uri.endsWith("css")) {
            contentType = TEXT_CSS;
        } else if (uri.endsWith("png")) {
            contentType = IMAGE_PNG;
        } else {
            contentType = APPLICATION_OCTET_STREAM;
        }
        return contentType;
    }

    protected void postHandle(HttpResponse httpResponse) {

    }
}
