package dev.paoding.longan.channel.http;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Component
public class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {
    private int maxContentLength;
    @Resource
    private HttpServerHandler httpServerHandler;
    @Resource
    private Environment env;

    @PostConstruct
    private void init() {
        try {
            maxContentLength = Integer.parseInt(env.getProperty("longan.http.max_content_length", "104857600"));
        } catch (NumberFormatException e) {
            throw new RuntimeException("http.max_content_length must be number");
        }
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
//        pipeline.addLast(new LongByteToMessageDecoder());
//        pipeline.addLast(new LoggingHandler(LogLevel.INFO));
//        pipeline.addLast(httpServerHandler);
//        pipeline.addLast(loggingHandler);
//        pipeline.addLast(new LoggingHandler(LogLevel.INFO));
        pipeline.addLast(new HttpServerCodec());
        //如果不聚合，header 和 body 会分开，一个完整的Http请求需要进行1+N次读取：1次HttpRequest读取，N次HttpContent读取
        pipeline.addLast(new HttpObjectAggregator(maxContentLength));
        //是否支持 Expect: 100-continue
//        pipeline.addLast(new HttpServerExpectContinueHandler());
        //压缩 https://blog.csdn.net/flyinmind/article/details/130243747
//        pipeline.addLast(new HttpContentCompressor());
        pipeline.addLast(new ChunkedWriteHandler());
//        pipeline.addLast(new WebSocketServerProtocolHandler("/ws",true));
        pipeline.addLast(new LonganWebSocketServerProtocolHandler("/ws",true));
        pipeline.addLast(httpServerHandler);
//        pipeline.addLast(new WebSocketServerHandler());
//        pipeline.addLast(new HttpServerHandler(httpFilter));
//        pipeline.addLast(businessGroup, new HttpServerHandler(httpFilter));
    }
}
