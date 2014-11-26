package com.origingame.business.echo.action;

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

    /**
     * 正常请求
     * @throws Exception
     */
    @Test
    public void test() throws Exception {
        ClientSession clientSession = null;

        try {
            clientSession = new ClientSession("localhost", 8080);
            clientSession.openConnection();
            clientSession.shakeHand();

            //请求10次
            for (int i = 0; i < 10; i++) {
                checkConnectionWork(clientSession);
            }

        } finally {
            if(clientSession != null) {
                clientSession.destroy();
            }
        }
    }


    /**
     * 测试一次会话(一次握手),多次tcp连接
     * @throws Exception
     */
    @Test
    public void testMultipleTcpConnectionInOneSession() throws Exception {
        ClientSession clientSession = null;

        try {
            clientSession = new ClientSession("localhost", 8080);
            clientSession.openConnection();
            clientSession.shakeHand();
            int sessionId = clientSession.getSessionId();
            int requestId = clientSession.currentRequestId();

            checkConnectionWork(clientSession);
            assertThat(clientSession.currentRequestId(), is(requestId + 1));

            //重新建立tcp连接
            clientSession.openConnection();

            checkConnectionWork(clientSession);

            assertThat(clientSession.getSessionId(), is(sessionId));
            assertThat(clientSession.currentRequestId(), is(requestId + 2));

        } finally {
            if(clientSession != null) {
                clientSession.destroy();
            }
        }
    }

    /**
     * 测试重复握手的时候带着不同的publicKey,服务器会生成新的session
     * @throws Exception
     */
    @Test
    public void testMultipleTcpConnectionInOneSession1() throws Exception {
        ClientSession clientSession = null;

        try {
            clientSession = new ClientSession("localhost", 8080);
            clientSession.openConnection();
            clientSession.shakeHand();
            int sessionId = clientSession.getSessionId();

            checkConnectionWork(clientSession);

            //重新握手
            clientSession.shakeHand();
            assertThat(clientSession.getSessionId(), is(sessionId + 1));

            checkConnectionWork(clientSession);
            assertThat(clientSession.getSessionId(), is(sessionId + 1));

        } finally {
            if(clientSession != null) {
                clientSession.destroy();
            }
        }
    }



    private void checkConnectionWork(ClientSession clientSession) {
        EchoProtos.Echo.Builder echo = EchoProtos.Echo.newBuilder();
        String msg = "你好啊123";
        echo.setMessage(msg);
        ClientResponseWrapper echoResponse = clientSession.sendMessage(echo.build());

        EchoProtos.Echo echoResponseMessage = (EchoProtos.Echo) echoResponse.getMessage();

        assertThat(echoResponseMessage.getMessage(), is(msg));
    }


}
