package dev.paoding.longan.channel.http;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * ChannelHandler 生命周期
 * handlerAdded: handler被添加到channel的pipeline
 * channelRegistered: channel注册到NioEventLoop
 * channelActive: channel准备就绪
 * channelRead: channel中有可读的数据
 * channelReadComplete: channel读数据完成
 * channelInactive: channel被关闭
 * channelUnregistered: channel取消和NioEventLoop的绑定
 * handlerRemoved: handler从channel的pipeline中移除
 */
@Component
@ChannelHandler.Sharable
public class HttpServerHandler extends ChannelInboundHandlerAdapter {
    private final Logger logger = LoggerFactory.getLogger(HttpServerHandler.class);
    @Resource
    private HttpHandler httpHandler;
    @Resource
    private WebSocketHandler webSocketHandler;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) {
        if (message instanceof FullHttpRequest request) {
            httpHandler.channelRead(ctx, request);
        } else if (message instanceof TextWebSocketFrame frame) {
            webSocketHandler.channelRead(ctx, frame);
        } else if (message instanceof BinaryWebSocketFrame frame) {
            webSocketHandler.channelRead(ctx, frame);
        } else {
            ctx.fireChannelRead(message);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object event) {
        if (event instanceof WebSocketServerProtocolHandler.HandshakeComplete handshake) {
            webSocketHandler.open(ctx, handshake.requestUri());
        } else if (event instanceof CloseWebSocketFrame) {
            webSocketHandler.close(ctx);
        } else {
            ctx.fireUserEventTriggered(event);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("connection cause exception", cause);
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
        logger.info("channel active" + ctx.channel().id());
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        logger.info("channel inactive" + ctx.channel().id());
    }
}
