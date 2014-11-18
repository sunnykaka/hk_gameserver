package com.origingame.server;


import com.google.protobuf.ByteString;
import com.origingame.server.model.GameSessionProtos;
import com.origingame.util.World;
import com.origingame.util.crypto.AES;
import com.origingame.util.crypto.RSA;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import redis.clients.jedis.Jedis;

import java.nio.charset.Charset;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;


/**
 * Unit test for simple App.
 */
@Test
public class RedisTest {

    private static Jedis jedis = World.getConnection();
    public void init() {
        System.out.println("before");
    }



    @Test
    public void test() throws Exception{

        int id = 1;
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

        byte[] bytes = jedis.get("not:exist:key".getBytes("ISO8859-1"));
        System.out.println(bytes);
    }


}
