package com.origingame.server.util;

import redis.clients.jedis.Jedis;

/**
 * User: Liub
 * Date: 2014/11/13
 */
public class IdGenerator {

    public static int incrSessionId(Jedis jedis) {
        return jedis.incr(RedisUtil.buildKey("session", "incr")).intValue();
    }


    public static int incrIdWithSession(Jedis jedis, int sessionId) {
        return jedis.hincrBy(RedisUtil.buildKey("session", "id", "incr"), String.valueOf(sessionId), 1).intValue();
    }

}
