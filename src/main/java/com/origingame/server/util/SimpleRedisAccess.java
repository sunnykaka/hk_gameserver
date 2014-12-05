package com.origingame.server.util;

import com.origingame.server.context.GameContextHolder;
import com.origingame.server.dao.DbMediator;
import org.springframework.util.ClassUtils;
import redis.clients.jedis.Jedis;

/**
 * User: Liub
 * Date: 2014/12/5
 */
public class SimpleRedisAccess {

    private static final Long SUCCESS_LONG = Long.valueOf(1L);

    public static boolean createIndex(Class clazz, String fieldName, String fieldValue, String targetValue) {
        DbMediator dbMediator = GameContextHolder.getDbMediator();
        Jedis jedis = dbMediator.selectCenterDb().getJedis();

        String key = RedisUtil.buildKey("i", clazz.getSimpleName(), fieldName);

        return SUCCESS_LONG.equals(jedis.hset(key, fieldValue, targetValue));
    }

    public static boolean createIndexNX(Class clazz, String fieldName, String fieldValue, String targetValue) {
        DbMediator dbMediator = GameContextHolder.getDbMediator();
        Jedis jedis = dbMediator.selectCenterDb().getJedis();

        String key = RedisUtil.buildKey("i", clazz.getSimpleName(), fieldName);

        return SUCCESS_LONG.equals(jedis.hsetnx(key, fieldValue, targetValue));
    }

    public static String getIndexValue(Class clazz, String fieldName, String fieldValue) {
        DbMediator dbMediator = GameContextHolder.getDbMediator();
        Jedis jedis = dbMediator.selectCenterDb().getJedis();

        String key = RedisUtil.buildKey("i", clazz.getSimpleName(), fieldName);

        return jedis.hget(key, fieldValue);
    }


}
