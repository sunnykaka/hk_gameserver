package com.origingame.server.main;

import com.origingame.server.netty.NettyChannelInitializer;
import com.origingame.server.netty.NettyGameProtocolReceiver;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User: liubin
 * Date: 14-2-6
 */
public class NettyGameServer {

    private static final Logger log = LoggerFactory.getLogger(NettyGameServer.class);

    private int port;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private AtomicBoolean initialized = new AtomicBoolean(false);
    private Channel serverChannel;

    public NettyGameServer(int port) {
        this.port = port;
    }

    private Channel accept(final ServerBootstrap b) throws InterruptedException{
        serverChannel = b.bind(port).sync().channel();

        serverChannel.closeFuture().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                log.info("server closed.");
                b.childGroup().shutdownGracefully();
                b.group().shutdownGracefully();
            }
        });

        return serverChannel;
    }

    public Channel accept(ChannelInitializer<SocketChannel> channelInitializer) throws InterruptedException {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();

        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler())
                .childHandler(channelInitializer)
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        return accept(b);
    }

    public void start() throws InterruptedException {
        if(!initialized.compareAndSet(false, true)) return;
        accept(new NettyChannelInitializer(new NettyGameProtocolReceiver()));
    }

    public void stop() {
        if(serverChannel != null) {
            serverChannel.close();
        }
//        if(workerGroup != null) {
//            workerGroup.shutdownGracefully();
//        }
//        if(bossGroup != null) {
//            bossGroup.shutdownGracefully();
//        }
    }
}
