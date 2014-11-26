package com.origingame.server;


import com.google.protobuf.ByteString;
import com.origingame.server.dao.ServerPersistenceResolver;
import com.origingame.persist.GameSessionProtos;
import com.origingame.server.main.World;
import org.junit.*;
import redis.clients.jedis.Jedis;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;


/**
 * Unit test for simple App.
 */
public class RedisTest {


    @BeforeClass
    public static void init() throws Exception {
        System.out.println("before");
        World.getInstance().init();
    }

    @AfterClass
    public static void destroy() throws Exception {
        System.out.println("after");
        World.getInstance().destroy();
    }

    @Test
    public void test() throws Exception{

        World.getInstance().init();

        int id = 1;
        Jedis jedis = ServerPersistenceResolver.getInstance().selectPlayerDb(id).getJedis();

        String password = "密码";
        String deviceId = "你好啊";
        GameSessionProtos.GameSessionModel.Builder gameSessionBuilder = GameSessionProtos.GameSessionModel.newBuilder();
        gameSessionBuilder.setId(id);
        gameSessionBuilder.setPasswordKey(ByteString.copyFrom(password.getBytes("UTF-8")));
        gameSessionBuilder.setDeviceId(deviceId);
        System.out.println("set resp:" + jedis.set("session:1".getBytes("ISO8859-1"), gameSessionBuilder.build().toByteArray())) ;

        byte[] bytes = jedis.get("session:1".getBytes("ISO8859-1"));
        GameSessionProtos.GameSessionModel gameSessionModel = GameSessionProtos.GameSessionModel.parseFrom(bytes);
        assertThat(gameSessionModel.getId(), is(id));
        assertThat(gameSessionModel.getPasswordKey().toByteArray(), is(password.getBytes("UTF-8")));
        assertThat(gameSessionModel.getDeviceId(), is(deviceId));
        System.out.println(gameSessionModel.getPasswordKey().toStringUtf8());
        System.out.println(gameSessionModel.getDeviceId());
    }

    @Test
    public void test2() throws Exception{

        World.getInstance().init();

        Jedis jedis = ServerPersistenceResolver.getInstance().selectCenterDb().getJedis();

        byte[] bytes = jedis.get("not:exist:key".getBytes("ISO8859-1"));
        System.out.println(bytes);
    }


}
