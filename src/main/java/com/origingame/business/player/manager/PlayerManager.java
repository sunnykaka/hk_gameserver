package com.origingame.business.player.manager;

import com.origingame.business.player.dao.PlayerDao;
import com.origingame.business.player.mock.CenterPlayer;
import com.origingame.business.player.mock.PlayerCenterMock;
import com.origingame.business.player.model.Player;
import com.origingame.exception.GameBusinessException;
import com.origingame.message.BaseMsgProtos;
import com.origingame.message.RegisterProtos;
import com.origingame.server.context.GameContext;
import com.origingame.server.main.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: Liub
 * Date: 2014/12/2
 */
public class PlayerManager {

    private static final Logger log = LoggerFactory.getLogger(PlayerManager.class);

    private PlayerCenterMock playerCenterMock = World.getBean(PlayerCenterMock.class);

    private PlayerDao playerDao = World.getBean(PlayerDao.class);

    public Player register(GameContext ctx, RegisterProtos.RegisterReq message) {

        boolean trial = message.getTrial();
        CenterPlayer centerPlayer;
        if(trial) {
            centerPlayer = playerCenterMock.registerTrailPlayer(ctx);
        } else {
            centerPlayer = playerCenterMock.registerPlayer(ctx, message.getUsername(), message.getPassword());
        }

        if(centerPlayer == null) {
            throw new GameBusinessException(BaseMsgProtos.ResponseStatus.FAILED);
        }

        return createPlayer(ctx, centerPlayer);

    }

    private Player createPlayer(GameContext ctx, CenterPlayer centerPlayer) {
        Player player = playerDao.createByCenterPlayer(ctx.getDbMediator(), centerPlayer);
        playerDao.save(ctx.getDbMediator(), player);
        return player;

    }

    public Player login(GameContext ctx, String username, String password) {

        CenterPlayer centerPlayer = playerCenterMock.login(ctx, username, password);

        Player player = playerDao.getByOuterId(ctx.getDbMediator(), centerPlayer.getCenterPlayerId());
        if(player == null) {
            log.error("根据outerId[%s]没有找到player", centerPlayer.getCenterPlayerId());
            throw new GameBusinessException(BaseMsgProtos.ResponseStatus.FAILED);
        }

//        ctx.getSession().bindPlayer(player.getId());
        player.getProperty().get().setSessionId(ctx.getSession().getBuilder().getId());
        player.getProperty().get().setLastLoginTime(World.now().getTime());
        player.getProperty().markUpdated();
        ctx.getSession().bindPlayer(player.getId());
        playerDao.save(ctx.getDbMediator(), player);

        return player;

    }
}
