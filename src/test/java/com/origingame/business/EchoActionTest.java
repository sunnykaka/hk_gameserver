package com.origingame.business;

import com.origingame.BaseNettyTest;
import com.origingame.client.main.ClientSession;
import com.origingame.client.protocol.ClientResponseWrapper;
import com.origingame.message.EchoProtos;
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
            clientSession.shakeHand();

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
