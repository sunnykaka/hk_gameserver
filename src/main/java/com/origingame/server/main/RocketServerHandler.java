package com.origingame.server.main;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * User: liubin
 * Date: 14-2-6
 */
public class RocketServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channel registered:" + ctx.name());

    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channel unregistered:" + ctx.name());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf byteBuf = (ByteBuf) msg;
//        System.out.println(byteBuf.toString(CharsetUtil.UTF_8));
//        ctx.writeAndFlush(byteBuf);
        System.out.println(((Byte)byteBuf.getByte(0)).intValue());
//        try {
//        } finally {
//            ReferenceCountUtil.release(msg);
//        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}
