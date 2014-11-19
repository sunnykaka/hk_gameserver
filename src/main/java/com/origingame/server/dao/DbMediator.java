package com.origingame.server.dao;

import com.origingame.server.dao.jaxb.ServerPersistence;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Map;

/**
 * User: Liub
 * Date: 2014/11/18
 */
public class DbMediator {

    private ServerPersistenceResolver serverPersistenceResolver = ServerPersistenceResolver.getInstance();

    private Map<Integer, PlayerDb> playerDbMap = new HashMap<>();

    private CenterDb centerDb;

    public PlayerDb selectPlayerDb(int playerOrSessionId) {

        int index = serverPersistenceResolver.findPlayerDbIndexById(playerOrSessionId);
        PlayerDb playerDb = playerDbMap.get(index);
        if(playerDb == null) {
            playerDb = serverPersistenceResolver.selectPlayerDb(playerOrSessionId);
            playerDbMap.put(index, playerDb);
        }
        return playerDb;
    }

    public CenterDb selectCenterDb() {
        if(centerDb == null) {
            centerDb = serverPersistenceResolver.selectCenterDb();
        }
        return centerDb;
    }

    public void close() {
        if(centerDb != null) {
            centerDb.close();
        }
        for(PlayerDb playerDb : playerDbMap.values()) {
            playerDb.close();
        }
    }

}
