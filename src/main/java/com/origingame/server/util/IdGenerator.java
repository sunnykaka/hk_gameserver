package com.origingame.server.util;

import redis.clients.jedis.Jedis;

/**
 * User: Liub
 * Date: 2014/11/13
 */
public class IdGenerator {

    public static int nextSessionId(Jedis jedis) {
        return jedis.incr(RedisUtil.buildKey("session", "next")).intValue();
    }


    public static int nextIdWithSession(Jedis jedis, int sessionId) {
        return jedis.hincrBy(RedisUtil.buildKey("session", "id", "next"), String.valueOf(sessionId), 1).intValue();
    }

}
