package com.origingame.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Date;

/**
 * User: Liub
 * Date: 2014/11/13
 */
public class World {

    private static JedisPool jedisPool;

    public static Date now() {
        return new Date();
    }

    public static synchronized void init() {
        jedisPool = new JedisPool("192.168.131.128", 6379);
    }

    public static Jedis getConnection() {
        return jedisPool.getResource();
    }

    public static void closeConnection(Jedis jedis) {
        jedisPool.returnResource(jedis);
    }

}
