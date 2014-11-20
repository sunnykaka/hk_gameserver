package com.origingame.server.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: liubin
 * Date: 14-2-6
 */
public class ServerMain {

    private static final Logger log = LoggerFactory.getLogger(ServerMain.class);

    public static void main(String[] args) throws Exception {

        int port;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 8080;
        }

        World.getInstance().init();
        NettyGameServer gameServer = new NettyGameServer(port);
        gameServer.start();

    }

    private void init() {
//        DescriptorRegistry.getInstance().init();
//        MessageHandlerRegistry.getInstance().init();
    }

}
