package com.origingame.server.netty;

import com.origingame.server.protocol.GameProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;


public class GameProtocolEncoder extends MessageToByteEncoder<GameProtocol> {

    @Override
    protected void encode(ChannelHandlerContext ctx, GameProtocol msg, ByteBuf out) throws Exception {
        msg.encode(out);
    }
}