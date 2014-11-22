package com.origingame;

import com.origingame.client.main.NettyGameClient;
import com.origingame.server.main.NettyGameServer;
import com.origingame.server.main.World;
import io.netty.channel.Channel;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static int idNumber = 0;

    @BeforeClass
    public static void init() {
        World.getInstance().init();
    }

    @AfterClass
    public static void destroy() {
        World.getInstance().destroy();
    }

    protected Channel initClient() throws InterruptedException, IOException {
        NettyGameClient client = new NettyGameClient(host, port);
        Channel channel = client.start();
        return channel;
    }

    protected void initServer() throws InterruptedException {
        NettyGameServer server = new NettyGameServer(port);
        server.start();
    }

    protected static synchronized int generateId() {
        return ++idNumber;
    }

}
