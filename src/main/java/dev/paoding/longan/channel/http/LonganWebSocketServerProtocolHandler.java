package dev.paoding.longan.channel.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.*;

import java.util.List;

public class LonganWebSocketServerProtocolHandler extends WebSocketServerProtocolHandler {

    public LonganWebSocketServerProtocolHandler(String websocketPath, boolean checkStartsWith) {
        super(websocketPath, checkStartsWith);
    }

    protected void decode(ChannelHandlerContext ctx, WebSocketFrame frame, List<Object> out) throws Exception {
        if (frame instanceof CloseWebSocketFrame) {
            ctx.fireUserEventTriggered(new CloseWebSocketFrame());
        }
        super.decode(ctx, frame, out);
    }
}
