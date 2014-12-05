package com.origingame.business.player.mock;

import com.origingame.server.context.GameContextHolder;
import redis.clients.jedis.Jedis;

/**
 * User: Liub
 * Date: 2014/12/2
 */
public class PlayerCenterMock {


    public CenterPlayer registerTrailPlayer() {

        Jedis jedis = GameContextHolder.getDbMediator().selectCenterDb().getJedis();
        CenterPlayer centerPlayer = CenterPlayer.create(jedis, null, null, true);
        return centerPlayer;

    }

    public CenterPlayer registerPlayer(String username, String password) {

        Jedis jedis = GameContextHolder.getDbMediator().selectCenterDb().getJedis();
        CenterPlayer centerPlayer = CenterPlayer.create(jedis, username, password, false);
        return centerPlayer;
    }

    public CenterPlayer login(String username, String password) {

        Jedis jedis = GameContextHolder.getDbMediator().selectCenterDb().getJedis();
        CenterPlayer centerPlayer = CenterPlayer.load(jedis, username, password);
        return centerPlayer;
//        PlayerDao.load(ctx.getDbMediator(), )

    }
}
