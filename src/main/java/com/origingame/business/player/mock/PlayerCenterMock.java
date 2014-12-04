package com.origingame.business.player.mock;

import com.origingame.business.player.dao.PlayerDao;
import com.origingame.server.context.GameContext;
import redis.clients.jedis.Jedis;

/**
 * User: Liub
 * Date: 2014/12/2
 */
public class PlayerCenterMock {


    public CenterPlayer registerTrailPlayer(GameContext ctx) {

        Jedis jedis = ctx.getDbMediator().selectCenterDb().getJedis();
        CenterPlayer centerPlayer = CenterPlayer.create(jedis, null, null, true);
        return centerPlayer;

    }

    public CenterPlayer registerPlayer(GameContext ctx, String username, String password) {

        Jedis jedis = ctx.getDbMediator().selectCenterDb().getJedis();
        CenterPlayer centerPlayer = CenterPlayer.create(jedis, username, password, false);
        return centerPlayer;
    }

    public CenterPlayer login(GameContext ctx, String username, String password) {

        Jedis jedis = ctx.getDbMediator().selectCenterDb().getJedis();
        CenterPlayer centerPlayer = CenterPlayer.load(jedis, username, password);
        return centerPlayer;
//        PlayerDao.load(ctx.getDbMediator(), )

    }
}
