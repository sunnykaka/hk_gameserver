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

/**
 * User: liubin
 * Date: 14-2-6
 */
public class NettyGameServer {

    private static final Logger log = LoggerFactory.getLogger(NettyGameServer.class);

    private int port;

    public NettyGameServer(int port) {
        this.port = port;
    }

    private Channel accept(final ServerBootstrap b) throws InterruptedException{
        Channel channel = b.bind(port).sync().channel();

        channel.closeFuture().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                log.info("server closed.");
                b.childGroup().shutdownGracefully();
                b.group().shutdownGracefully();
            }
        });

        return channel;
    }

    public Channel accept(ChannelInitializer<SocketChannel> channelInitializer) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler())
                .childHandler(channelInitializer)
                .option(ChannelOption.SO_BACKLOG, 128)
//                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        return accept(b);
    }

    public void start() throws InterruptedException {
        accept(new NettyChannelInitializer(new NettyGameProtocolReceiver()));
    }
}
