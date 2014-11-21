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

    BufferedReader input;


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

    public Channel connect(String host, int port, ChannelInitializer<SocketChannel> channelInitializer) throws InterruptedException {
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

    public Channel connect(String host, int port) throws InterruptedException {
        return connect(host, port, new NettyChannelInitializer(new NettyGameProtocolReceiver()));
    }

    public void start() throws IOException, InterruptedException {
        displayWelcomeInfo();
        this.channel = connect(host, port);
        System.out.println(String.format("connect success"));
        this.input = new BufferedReader(new InputStreamReader(System.in));
        String command = input.readLine();
        if("QUIT".equalsIgnoreCase(command)) {
            exit();
        }
        if("CONNECT".equalsIgnoreCase(command)) {
            tryConnect();
        } else {
            System.out.println("unknown command, please input 'connect' or 'quit'");
        }
    }

    private void tryConnect() throws IOException{
        System.out.print("username: ");
        String username = input.readLine();
        System.out.print("password: ");
        String password = input.readLine();

    }

    private void exit() {
        System.out.println("bye");
        if(bootstrap != null) {
            bootstrap.group().shutdownGracefully();
        }
        if(input != null) {
            try {
                input.close();
            } catch (IOException e) {
                log.error("", e);
            }
        }
        System.exit(0);
    }

    private void displayWelcomeInfo() {
        System.out.println("welcome to the Game!");
        System.out.println(String.format("connect to %s:%d", host, port));
    }

    public Channel getChannel() {
        return channel;
    }
}