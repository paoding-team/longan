package dev.paoding.longan.channel.http;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.TEXT_PLAIN;

@Component
public class HttpHandler {
    private final Logger logger = LoggerFactory.getLogger(HttpHandler.class);
    private final ExecutorService executorService;
    @Resource
    private HttpServiceHandler httpServiceHandler;
    @Resource
    private DocServiceHandler docServiceHandler;
    @Value("${longan.http.cross-origin:false}")
    private Boolean enableCrossOrigin;

    {
        ThreadFactory threadFactory = Thread.ofVirtual().name("http-thread-", 0).uncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                logger.error(throwable.getMessage());
            }
        }).factory();
        executorService = Executors.newThreadPerTaskExecutor(threadFactory);
    }

    public void channelRead(ChannelHandlerContext ctx, FullHttpRequest request) {
        executorService.execute(() -> {
            try {
                FullHttpResponse response;
                if (request.method() == HttpMethod.OPTIONS) {
                    response = new DefaultFullHttpResponse(request.protocolVersion(), HttpResponseStatus.NO_CONTENT);
                    if (enableCrossOrigin) {
                        HttpHeaders httpHeaders = response.headers();
                        httpHeaders.set("Access-Control-Allow-Origin", "*");
                        httpHeaders.set("Access-Control-Allow-Methods", "*");
                        httpHeaders.set("Access-Control-Allow-Headers", "*");
                        httpHeaders.set("Access-Control-Allow-Credentials", "true");
                    }
                } else {
                    String uri = request.uri();
                    if (uri.startsWith("/api/")) {
                        response = httpServiceHandler.channelRead(ctx, request);
                    } else if (uri.startsWith("/doc/")) {
                        response = docServiceHandler.channelRead(ctx, request);
                    } else {
                        String message = "Not found " + uri;
                        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
                        response = new DefaultFullHttpResponse(request.protocolVersion(), HttpResponseStatus.NOT_FOUND, Unpooled.wrappedBuffer(bytes));
                        response.headers().set(CONTENT_TYPE, TEXT_PLAIN);
                        HttpUtil.setContentLength(response, bytes.length);
                    }
                }
                boolean keepAlive = HttpUtil.isKeepAlive(request);
                HttpUtil.setKeepAlive(response, keepAlive);
                ChannelFuture channelFuture = ctx.writeAndFlush(response);
                if (!keepAlive) {
                    channelFuture.addListener(ChannelFutureListener.CLOSE);
                }
            } finally {
                ReferenceCountUtil.release(request);
            }
        });
    }
}
