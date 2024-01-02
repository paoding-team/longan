package dev.paoding.longan.channel.http;

import dev.paoding.longan.doc.DocumentService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.AsciiString;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class DocServiceHandler extends AbstractServiceHandler {

    public FullHttpResponse channelRead(ChannelHandlerContext ctx, FullHttpRequest request) {
        String uri = request.uri();
        if (uri.equals("/doc") || uri.equals("/doc/") || uri.equals("/doc/index.html")) {
            ClassPathResource classPathResource = new ClassPathResource("doc/index.html");
            String text = new String(copyToByteArray(classPathResource), StandardCharsets.UTF_8);
            return writeHtml(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, text);
        } else if (uri.equals("/doc/doc.json")) {
            String json = DocumentService.load();
            return writeJson(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, json);
        } else if (uri.equals("/doc/models.json")) {
            String json = DocumentService.getModels();
            return writeJson(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, json);
        } else if (uri.equals("/doc/methods.json")) {
            String json = DocumentService.getMethods();
            return writeJson(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, json);
        } else if (uri.startsWith("/doc/method/")) {
            String methodName = uri.substring(11);
            String json = DocumentService.getMethod(methodName);
            return writeJson(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, json);
        }

        ClassPathResource classPathResource = new ClassPathResource(uri.substring(1));
        if (!classPathResource.exists()) {
            classPathResource = new ClassPathResource("doc/404.html");
            String text = new String(copyToByteArray(classPathResource), StandardCharsets.UTF_8);
            return writeHtml(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND, text);
        } else {
            byte[] bytes = copyToByteArray(classPathResource);
            AsciiString contentType = getContentType(uri);
            return write(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, bytes, AsciiString.of(contentType));
        }
    }

    private byte[] copyToByteArray(ClassPathResource classPathResource) {
        try {
            return FileCopyUtils.copyToByteArray(classPathResource.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
