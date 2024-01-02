package dev.paoding.longan.channel.http;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.epoll.*;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.unix.UnixChannelOption;
import io.netty.util.internal.SystemPropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.concurrent.ThreadFactory;


@Component
public class HttpServer {
    private final Logger logger = LoggerFactory.getLogger(HttpServer.class);
    @Resource
    private ServerChannelInitializer serverChannelInitializer;
    @Value("${longan.http.port:8001}")
    private int port;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workGroup;
    private final ThreadFactory bossThreadFactory;
    private final ThreadFactory workThreadFactory;

    {
        bossThreadFactory = new ThreadFactoryBuilder().setNameFormat("boss-thread-%d").build();
        workThreadFactory = new ThreadFactoryBuilder().setNameFormat("work-thread-%d").build();
    }

    //todo 增加对Unix Domain Socket支持，ipc模式 https://github.com/netty/netty/pull/3344
    @PostConstruct
    public void startup() throws Exception {
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
        //探测内存泄漏点
//        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED);
        //调试时可以通过增加：-Dio.netty.leakDetectionLevel=PARANOID来保障对每次请求都做内存溢出检测
        String name = SystemPropertyUtil.get("os.name").trim();
        String version = SystemPropertyUtil.get("os.version");
        if (Epoll.isAvailable()) {
            logger.info("EPoll supported on {} {} system.", name, version);
            this.bossGroup = new EpollEventLoopGroup(bossThreadFactory);
            this.workGroup = new EpollEventLoopGroup(workThreadFactory);
            start(bossGroup, workGroup, EpollServerSocketChannel.class);
        } else if (KQueue.isAvailable()) {
            logger.info("KQueue supported on {} {} system.", name, version);
            this.bossGroup = new KQueueEventLoopGroup(bossThreadFactory);
            this.workGroup = new KQueueEventLoopGroup(workThreadFactory);
            start(bossGroup, workGroup, KQueueServerSocketChannel.class);
        } else {
            logger.info("NIO supported on {} {} system.", name, version);
            startNio();
        }
    }

    private void startNio() throws Exception {
        this.bossGroup = new NioEventLoopGroup();
        this.workGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workGroup).channel(NioServerSocketChannel.class)
                .childHandler(serverChannelInitializer)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        ChannelFuture future = bootstrap.bind(port).sync();
        if (!future.isSuccess()) {
            throw new Exception(String.format("Fail to bind on port = %d.", port), future.cause());
        }
        logger.info("Starting server at port {}.", port);
    }

    private void start(EventLoopGroup bossGroup, EventLoopGroup workerGroup, Class<? extends ServerSocketChannel> channelClass) throws Exception {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup).channel(channelClass)
                .childHandler(serverChannelInitializer)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(UnixChannelOption.SO_REUSEPORT, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(512 * 1024, 1024 * 1024))
                .childOption(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.ALLOW_HALF_CLOSURE, false);

        if (Epoll.isAvailable()) {
//            bootstrap.option(EpollChannelOption.SO_REUSEPORT, true);
            bootstrap.option(EpollChannelOption.IP_FREEBIND, false)
                    .option(EpollChannelOption.IP_TRANSPARENT, false)
                    .option(EpollChannelOption.EPOLL_MODE, EpollMode.EDGE_TRIGGERED)
                    .childOption(EpollChannelOption.TCP_CORK, false)
                    .childOption(EpollChannelOption.TCP_QUICKACK, true)
                    .childOption(EpollChannelOption.IP_TRANSPARENT, false)
                    .childOption(EpollChannelOption.EPOLL_MODE, EpollMode.EDGE_TRIGGERED);
        }
//        if (KQueue.isAvailable()) {
//            bootstrap.option(KQueueChannelOption.SO_REUSEPORT, true);
//        }

        int workThreadSize = Runtime.getRuntime().availableProcessors();
        for (int i = 0; i < workThreadSize; i++) {
            ChannelFuture future = bootstrap.bind(port).sync();
            if (!future.isSuccess()) {
                throw new Exception(String.format("Fail to bind on port = %d.", port), future.cause());
            }
        }
//        logger.info("Enabled TCP.SO_REUSEPORT option.");
        logger.info("Starting server on port {}.", port);
    }

    public void shutdown() {
        Thread.currentThread().setName("ApplicationShutdownHook");
        logger.info("Stop server on port {}.", port);
        if (workGroup != null) {
            workGroup.shutdownGracefully();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
    }


}