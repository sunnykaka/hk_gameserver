package com.origingame.server.netty;

import com.origingame.server.protocol.GameProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;


public class GameProtocolDecoder extends ByteToMessageDecoder{

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        GameProtocol protocol = GameProtocol.decode(in);
        if(protocol == null) return;
        out.add(protocol);
    }
}