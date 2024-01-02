package dev.paoding.longan.channel.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Component
public class WebSocketHandler {
    private final Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);
    public final static AttributeKey<WebSocketSession> WEB_SOCKET_SESSION_ATTRIBUTE_KEY = AttributeKey.valueOf("WEB_SOCKET_SESSION");
    @Resource
    private WebSocketListenerHandler webSocketListenerHandler;
    private final ExecutorService executorService;

    {
        ThreadFactory threadFactory = Thread.ofVirtual().name("websocket-thread-", 0).uncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                logger.error(throwable.getMessage());
            }
        }).factory();
        executorService = Executors.newThreadPerTaskExecutor(threadFactory);
    }

    public void channelRead(ChannelHandlerContext ctx, TextWebSocketFrame frame) {
        executorService.execute(() -> {
            try {
                WebSocketSession webSocketSession = ctx.channel().attr(WEB_SOCKET_SESSION_ATTRIBUTE_KEY).get();
                webSocketListenerHandler.onMessage(webSocketSession, frame.text());
            } finally {
                ReferenceCountUtil.release(frame);
            }
        });
    }

    public void channelRead(ChannelHandlerContext ctx, BinaryWebSocketFrame frame) {
        executorService.execute(() -> {
            try {
                WebSocketSession webSocketSession = ctx.channel().attr(WEB_SOCKET_SESSION_ATTRIBUTE_KEY).get();
                webSocketListenerHandler.onMessage(webSocketSession, frame.content().array());
            } finally {
                ReferenceCountUtil.release(frame);
            }
        });
    }

    public void close(ChannelHandlerContext ctx) {
        executorService.execute(() -> {
            WebSocketSession webSocketSession = ctx.channel().attr(WEB_SOCKET_SESSION_ATTRIBUTE_KEY).get();
            webSocketListenerHandler.onClose(webSocketSession);
        });
    }

    public void open(ChannelHandlerContext ctx, String requestUri) {
        executorService.execute(() -> {
            WebSocketSession webSocketSession = new WebSocketSession(ctx.channel(), requestUri);
            ctx.channel().attr(WEB_SOCKET_SESSION_ATTRIBUTE_KEY).set(webSocketSession);
            webSocketListenerHandler.onOpen(webSocketSession);
        });
    }


}
