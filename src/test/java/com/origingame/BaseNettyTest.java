package com.origingame;

import com.origingame.client.main.ClientSession;
import com.origingame.client.main.NettyGameClient;
import com.origingame.server.main.NettyGameServer;
import com.origingame.server.main.World;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;

import java.io.IOException;

/**
 *
 *
 * User: Liub
 * Date: 2014/11/21
 */
public abstract class BaseNettyTest {

    private final Logger log = LoggerFactory.getLogger(BaseNettyTest.class);

    protected String host = "localhost";
    protected int port = 8080;

    @BeforeTest
    public static void init() throws Exception {
        World.getInstance().init();
    }

    @AfterTest
    public static void destroy() throws Exception {
        World.getInstance().destroy();
    }

//    protected Channel initClient() throws InterruptedException, IOException {
//        NettyGameClient client = new NettyGameClient(host, port);
//        Channel channel = client.start();
//        return channel;
//    }
//
//    protected void initServer() throws InterruptedException {
//        NettyGameServer server = new NettyGameServer(port);
//        server.start();
//    }
//
//    protected static synchronized int generateId() {
//        return ++idNumber;
//    }


    protected ClientSession initSession() {
        ClientSession clientSession = new ClientSession(host, port);
        clientSession.openConnection();
        clientSession.shakeHand();
        return clientSession;
    }


}
