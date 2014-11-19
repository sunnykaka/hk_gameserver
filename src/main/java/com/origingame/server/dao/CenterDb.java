package com.origingame.server.dao;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * User: Liub
 * Date: 2014/11/19
 */
public class CenterDb extends Db {

    public CenterDb(JedisPool centerDbPool) {
        super(centerDbPool);
    }

}
