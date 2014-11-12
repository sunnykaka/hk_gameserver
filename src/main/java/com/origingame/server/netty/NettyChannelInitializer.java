package com.origingame.server.netty;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.concurrent.EventExecutorGroup;

/**
 * User: liubin
 * Date: 14-3-20
 */
public class NettyChannelInitializer extends ChannelInitializer<SocketChannel> {

    private EventExecutorGroup eventExecutorGroup;
    private ChannelHandler channelHandler;

    public NettyChannelInitializer(EventExecutorGroup eventExecutorGroup, ChannelHandler channelHandler) {
        this.eventExecutorGroup = eventExecutorGroup;
        this.channelHandler = channelHandler;
    }

    public NettyChannelInitializer(ChannelHandler channelHandler) {
        this(null, channelHandler);
    }

    public NettyChannelInitializer() {
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline().addLast(new GameProtocolDecoder());
        ch.pipeline().addLast(new GameProtocolEncoder());
        if(channelHandler != null) {
            if(eventExecutorGroup != null) {
                ch.pipeline().addLast(eventExecutorGroup, channelHandler);
            } else {
                ch.pipeline().addLast(channelHandler);
            }
        }
    }
}
