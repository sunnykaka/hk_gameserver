package com.origingame.client.main;

import com.origingame.server.netty.NettyChannelInitializer;
import com.origingame.server.netty.NettyGameProtocolReceiver;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class NettyGameClient {

    private static final Logger log = LoggerFactory.getLogger(NettyGameClient.class);

    private String host;

    private int port;

    private Bootstrap bootstrap;

    private Channel channel;

    public NettyGameClient(String host, int port) {
        this.host = host;
        this.port = port;
        init();
    }

    public NettyGameClient() {
        this("localhost", 8080);
    }

    private void init() {
    }

    public static void main(String[] args) throws Exception {
//        String host = args[0];
//        int port = Integer.parseInt(args[1]);
        String host = "localhost";
        int port = 8080;

        NettyGameClient client = new NettyGameClient(host, port);
        client.start();
    }

    private Channel connect(String host, int port, final Bootstrap b) throws InterruptedException {
        // Start the client.
        Channel channel = b.connect(host, port).sync().channel();
        channel.closeFuture().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                log.info("client closed.");
                b.group().shutdownGracefully();
            }
        });

        return channel;
    }

    private Channel connect(String host, int port, ChannelInitializer<SocketChannel> channelInitializer) throws InterruptedException {
        final EventLoopGroup workerGroup = new NioEventLoopGroup();

        Bootstrap b = new Bootstrap();
        b.group(workerGroup);
        b.channel(NioSocketChannel.class);
//        b.option(ChannelOption.SO_KEEPALIVE, true);
        b.option(ChannelOption.TCP_NODELAY, true);
        b.handler(channelInitializer);

        this.bootstrap = b;

        return connect(host, port, b);
    }

    private Channel connect(String host, int port) throws InterruptedException {
        return connect(host, port, new NettyChannelInitializer(new NettyGameProtocolReceiver()));
    }

    public Channel start() throws IOException, InterruptedException {
        channel = connect(host, port);
        return channel;
    }

    public void stop() throws IOException, InterruptedException {
        if(channel != null) {
            channel.close();
        }
    }


    public Channel getChannel() {
        return channel;
    }
}