package com.origingame.server.util;

import com.google.protobuf.Message;
import com.origingame.server.dao.DbMediator;
import redis.clients.jedis.Jedis;

/**
 * User: Liub
 * Date: 2014/11/13
 */
public class IdGenerator {

    public static int nextSessionId(DbMediator dbMediator) {
        return dbMediator.selectCenterDb().getJedis().incr(RedisUtil.buildKey("id", "next")).intValue();
    }

    public static int nextId(DbMediator dbMediator, Class<? extends Message> clazz) {
        return dbMediator.selectCenterDb().getJedis().incr(RedisUtil.buildKey("id", clazz.getSimpleName())).intValue();
    }



//    public static int nextIdWithSession(Jedis jedis, int sessionId) {
//        return jedis.hincrBy(RedisUtil.buildKey("session", "id", "next"), String.valueOf(sessionId), 1).intValue();
//    }

}
