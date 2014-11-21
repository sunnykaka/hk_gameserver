package com.origingame.server.dao;


import com.google.common.collect.Sets;
import com.origingame.server.action.ActionResolver;
import com.origingame.server.action.TestAction;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;


/**
 * Unit test for simple App.
 */
public class ServerPersistenceResolverTest {


    @BeforeClass
    public static void init() {
    }

    @AfterClass
    public static void destroy() {
    }

    @Test
    public void test() throws Exception{

        ServerPersistenceResolver.getInstance().init(this.getClass().getPackage().getName().replaceAll("\\.", "/") + "/test-server-persistence.xml");
//        ServerPersistenceResolver.getInstance().init("test-server-persistence.xml");

        assertThat(ServerPersistenceResolver.getInstance().playerRoundStep, is(100));
        assertThat(ServerPersistenceResolver.getInstance().playerShardSize, is(32));
        assertThat(ServerPersistenceResolver.getInstance().playerDbSize, is(2));
        assertThat(ServerPersistenceResolver.getInstance().realDbMap.size(), is(3));

        Collection<JedisPool> realDbList = ServerPersistenceResolver.getInstance().realDbMap.values();
        for(JedisPool pool : realDbList) {
            Jedis jedis = pool.getResource();
            jedis.select(1);
            jedis.flushDB();
        }

        CenterDb centerDb = ServerPersistenceResolver.getInstance().selectCenterDb();

        String key = "whatevercenter";
        String value = "111center";

        centerDb.getJedis().set(key, value);

        Jedis centerJedis = new Jedis("192.168.131.128", 6379);
        centerJedis.select(1);

        assertThat(centerJedis.get(key), is(value));

        Jedis playerJedis1 = new Jedis("192.168.131.128", 6380);
        Jedis playerJedis2 = new Jedis("192.168.131.128", 6381);
        playerJedis1.select(1);
        playerJedis2.select(1);

        checkIfSelectTheRightDb(0, playerJedis1);
        checkIfSelectTheRightDb(7, playerJedis1);
        checkIfSelectTheRightDb(100, playerJedis1);
        checkIfSelectTheRightDb(101, playerJedis1);
        checkIfSelectTheRightDb(1599, playerJedis1);

        checkIfSelectTheRightDb(1600, playerJedis2);
        checkIfSelectTheRightDb(1700, playerJedis2);
        checkIfSelectTheRightDb(2300, playerJedis2);
        checkIfSelectTheRightDb(2400, playerJedis2);
        checkIfSelectTheRightDb(2500, playerJedis2);
        checkIfSelectTheRightDb(3100, playerJedis2);

        checkIfSelectTheRightDb(3200, playerJedis1);
        checkIfSelectTheRightDb(4700, playerJedis1);

        checkIfSelectTheRightDb(4800, playerJedis2);

    }

    private void checkIfSelectTheRightDb(int id, Jedis manualSelectDb) {
        String key = "whatever:" + id;
        String value = "111" + id;
        PlayerDb playerDb = ServerPersistenceResolver.getInstance().selectPlayerDb(id);
        playerDb.getJedis().set(key, value);

        manualSelectDb.select(1);
        assertThat(manualSelectDb.get(key), is(value));
    }


}
