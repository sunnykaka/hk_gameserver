package com.origingame.server;


import com.google.protobuf.ByteString;
import com.origingame.business.player.dao.PlayerDao;
import com.origingame.business.player.model.Player;
import com.origingame.persist.GameSessionProtos;
import com.origingame.server.dao.DbMediator;
import com.origingame.server.dao.ServerPersistenceResolver;
import com.origingame.server.main.World;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import redis.clients.jedis.Jedis;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;


/**
 * Unit test for simple App.
 */
public class LookRedisDataTest {


    @BeforeTest
    public static void init() throws Exception {
        System.out.println("before");
        World.getInstance().init();
    }

    @AfterTest
    public static void destroy() throws Exception {
        System.out.println("after");
        World.getInstance().destroy();
    }

    @Test
    public void test() throws Exception{

        DbMediator dbMediator = new DbMediator();
        Player player = World.getBean(PlayerDao.class).load(dbMediator, 16);
        System.out.println(player.getProperty().get().getSessionId());

    }


}
