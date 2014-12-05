package com.origingame.server.util;

import com.origingame.server.dao.DbMediator;
import redis.clients.jedis.Jedis;

/**
 * User: Liub
 * Date: 2014/11/13
 */
public class IdGenerator {


    public static int nextId(DbMediator dbMediator, Class clazz) {
        return nextId(dbMediator.selectCenterDb().getJedis(), clazz);
    }

    public static int nextId(Jedis jedis, Class clazz) {
        return jedis.incr(RedisUtil.buildKey("id", clazz.getSimpleName())).intValue();
    }




//    public static int nextIdWithSession(Jedis jedis, int sessionId) {
//        return jedis.hincrBy(RedisUtil.buildKey("session", "id", "next"), String.valueOf(sessionId), 1).intValue();
//    }

}
