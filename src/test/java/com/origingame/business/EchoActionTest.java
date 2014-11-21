package com.origingame.business;

import com.google.protobuf.ByteString;
import com.origingame.BaseNettyTest;
import com.origingame.message.HandShakeProtos;
import com.origingame.server.message.MessageDispatcher;
import com.origingame.server.protocol.GameProtocol;
import com.origingame.server.protocol.ServerRequestWrapper;
import com.origingame.server.protocol.ResponseWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * User: Liub
 * Date: 2014/11/21
 */
public class EchoActionTest extends BaseNettyTest {

    private final Logger log = LoggerFactory.getLogger(EchoActionTest.class);

    @Test
    public void test() throws Exception {
//        initServer();

        Channel channel = initClient();

        HandShakeProtos.HandShakeReq.Builder handShakeReq = HandShakeProtos.HandShakeReq.newBuilder();
        handShakeReq.setPublicKey(ByteString.EMPTY);
        ServerRequestWrapper requestWrapper = ServerRequestWrapper.fromMessage(handShakeReq.build());

        ResponseWrapper response = MessageDispatcher.getInstance().request(requestWrapper);

        GameProtocol.Builder handshakeProtocol = GameProtocol.newBuilder();
        handshakeProtocol.setPhase(GameProtocol.Phase.HAND_SHAKE);
        ByteBuf byteBuf = Unpooled.buffer();
        handshakeProtocol.build().encode(byteBuf);
        channel.write(channel);
        channel.close();







    }



}
