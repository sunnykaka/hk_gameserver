package com.origingame.server.netty;

import com.origingame.server.message.MessageDispatcher;
import com.origingame.server.protocol.GameProtocol;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: liubin
 * Date: 14-2-6
 */
@ChannelHandler.Sharable
public class NettyGameProtocolReceiver extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(GameProtocol.class);

    MessageDispatcher messageDispatcher = MessageDispatcher.getInstance();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            messageDispatcher.receive(ctx.channel(), (GameProtocol) msg);
        } catch (Throwable e) {
            log.error("", e);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Netty发生IO异常,关闭与客户端的连接", cause);
        ctx.close();
    }

}
