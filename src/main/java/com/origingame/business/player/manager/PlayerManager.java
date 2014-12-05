package com.origingame.business.player.manager;

import com.origingame.business.player.dao.PlayerDao;
import com.origingame.business.player.mock.CenterPlayer;
import com.origingame.business.player.mock.PlayerCenterMock;
import com.origingame.business.player.model.Player;
import com.origingame.exception.GameBusinessException;
import com.origingame.message.BaseMsgProtos;
import com.origingame.message.RegisterProtos;
import com.origingame.server.context.GameContext;
import com.origingame.server.context.GameContextHolder;
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

    public Player register(RegisterProtos.RegisterReq message) {

        boolean trial = message.getTrial();
        CenterPlayer centerPlayer;
        if(trial) {
            centerPlayer = playerCenterMock.registerTrailPlayer();
        } else {
            centerPlayer = playerCenterMock.registerPlayer(message.getUsername(), message.getPassword());
        }

        if(centerPlayer == null) {
            throw new GameBusinessException(BaseMsgProtos.ResponseStatus.FAILED);
        }

        return createPlayer(centerPlayer);

    }

    private Player createPlayer(CenterPlayer centerPlayer) {
        Player player = playerDao.createByCenterPlayer(centerPlayer);
        playerDao.save(player);
        return player;

    }

    public Player login( String username, String password) {

        GameContext ctx = GameContextHolder.get();
        CenterPlayer centerPlayer = playerCenterMock.login(username, password);

        Player player = playerDao.getByOuterId(centerPlayer.getCenterPlayerId());
        if(player == null) {
            log.error("根据outerId[%s]没有找到player", centerPlayer.getCenterPlayerId());
            throw new GameBusinessException(BaseMsgProtos.ResponseStatus.FAILED);
        }

//        ctx.getSession().bindPlayer(player.getId());
        player.getProperty().get().setSessionId(ctx.getSession().getBuilder().getId());
        player.getProperty().get().setLastLoginTime(World.now().getTime());
        player.getProperty().markUpdated();
        ctx.getSession().bindPlayer(player.getId());
        playerDao.save(player);

        return player;

    }
}
