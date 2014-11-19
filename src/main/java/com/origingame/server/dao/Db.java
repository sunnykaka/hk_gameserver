package com.origingame.server.dao;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * User: Liub
 * Date: 2014/11/18
 */
public abstract class Db {

    protected Jedis jedis;

    protected JedisPool jedisPool;

    public Db(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
        this.jedis = jedisPool.getResource();
    }

    public void close() {
        jedisPool.returnResource(jedis);
    }

    public Jedis getJedis() {
        return jedis;
    }
}
