package com.origingame.business;

import com.google.protobuf.ByteString;
import com.origingame.BaseNettyTest;
import com.origingame.client.main.ClientSession;
import com.origingame.client.protocol.ClientRequestWrapper;
import com.origingame.client.protocol.ClientResponseWrapper;
import com.origingame.exception.GameException;
import com.origingame.message.EchoProtos;
import com.origingame.message.HandShakeProtos;
import com.origingame.server.message.MessageDispatcher;
import com.origingame.server.protocol.GameProtocol;
import com.origingame.server.protocol.ServerResponseWrapper;
import com.origingame.util.crypto.RSA;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;


/**
 *
 * 运行之前要先启动服务器
 * User: Liub
 * Date: 2014/11/21
 */
public class EchoActionTest extends BaseNettyTest {

    private final Logger log = LoggerFactory.getLogger(EchoActionTest.class);

    @Test
    public void test() throws Exception {
        ClientSession clientSession = null;

        try {
            clientSession = new ClientSession("localhost", 8080);
            clientSession.openConnection();
            clientSession.handShake();

            EchoProtos.Echo.Builder echo = EchoProtos.Echo.newBuilder();
            String msg = "你好啊123";
            echo.setMessage(msg);
            ClientResponseWrapper echoResponse = clientSession.sendMessage(echo.build());

            EchoProtos.Echo echoResponseMessage = (EchoProtos.Echo) echoResponse.getMessage();

            assertThat(echoResponseMessage.getMessage(), is(msg));

        } finally {
            if(clientSession != null) {
                clientSession.destroy();
            }
        }




    }



}
