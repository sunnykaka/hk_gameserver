package com.origingame.server.dao;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * User: Liub
 * Date: 2014/11/19
 */
public class PlayerDb extends Db {

    private int index;

    public PlayerDb(JedisPool jedisPool, int index) {
        super(jedisPool);
        this.index = index;
    }

}
