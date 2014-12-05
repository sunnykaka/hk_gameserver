package com.origingame.server.context;

import com.origingame.business.player.dao.PlayerDao;
import com.origingame.business.player.model.Player;
import com.origingame.exception.GameBusinessException;
import com.origingame.message.BaseMsgProtos;
import com.origingame.server.action.annotation.CheckPlayer;
import com.origingame.server.dao.DbMediator;
import com.origingame.exception.GameProtocolException;
import com.origingame.server.main.World;
import com.origingame.server.protocol.GameProtocol;
import com.origingame.server.protocol.ServerRequestWrapper;
import com.origingame.server.protocol.ServerResponseWrapper;
import com.origingame.server.session.GameSession;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: Liub
 * Date: 2014/11/13
 */
public class GameContext {

    private static final Logger log = LoggerFactory.getLogger(GameContext.class);

    private PlayerDao playerDao = World.getBean(PlayerDao.class);

    private GameSession session;

    private Channel channel;

    private ServerRequestWrapper request;

    private ServerResponseWrapper response;

    private DbMediator dbMediator;

    private Player player;

    public GameContext() {
        this.dbMediator = new DbMediator();
    }

    public void init(Channel channel, GameProtocol protocol) {
        this.channel = channel;
        this.request = ServerRequestWrapper.fromProtocol(channel, protocol);
        int sessionId = protocol.getSessionId();
        if(sessionId > 0) {
            this.session = GameSession.load(this, sessionId);
        }
    }

    public void setSession(GameSession session) {
        this.session = session;
    }

    public void setRequest(ServerRequestWrapper request) {
        this.request = request;
    }

    public void setResponse(ServerResponseWrapper response) {
        this.response = response;
    }

    public GameSession getSession() {
        return session;
    }

    public Channel getChannel() {
        return channel;
    }

    public ServerRequestWrapper getRequest() {
        return request;
    }

    public ServerResponseWrapper getResponse() {
        return response;
    }

    public DbMediator getDbMediator() {
        return dbMediator;
    }

    /**
     *
     * @param checkPlayer
     */
    public void checkSession(CheckPlayer checkPlayer) {
        GameProtocol protocol = request.getProtocol();

        if(log.isDebugEnabled()) {
            log.debug("校验session: protocolId[{}], sessionLastId[{}], sessionPlayerId[{}], sessionId[{}], playerSessionId[{}]",
                    protocol.getId(),
                    session == null ? 0 : session.getBuilder().getLastId(),
                    session == null ? 0 : session.getBuilder().getPlayerId(),
                    session == null ? 0 : session.getBuilder().getId(),
                    player == null ? 0 : player.getProperty().get().getSessionId()
                    );
        }

        if(session == null) {
            throw new GameProtocolException(GameProtocol.Status.INVALID_SESSION_ID, protocol);
        }
        if(protocol.getId() <= 0) {
            throw new GameProtocolException(GameProtocol.Status.INVALID_ID, protocol);
        }
        if(protocol.getId() <= session.getBuilder().getLastId()) {
            throw new GameProtocolException(GameProtocol.Status.REPEAT_ID, protocol);
        }

        if(checkPlayer != null) {
            initPlayer(request.getPlayerId());

            if(session.getBuilder().getPlayerId() <= 0) {
                throw new GameBusinessException(BaseMsgProtos.ResponseStatus.SESSION_NOT_BIND_TO_PLAYER_YET);
            }
            //校验请求的玩家id要与session绑定的玩家id相同
            if(request.getPlayerId() != session.getBuilder().getPlayerId()) {
                throw new GameBusinessException(BaseMsgProtos.ResponseStatus.MULTI_PLAYER_WITH_SESSION);
            }
            //校验玩家id没有对应其他session
            int previousSessionId = player.getProperty().get().getSessionId();
            if(previousSessionId > 0 && previousSessionId != session.getBuilder().getId()) {
                throw new GameBusinessException(BaseMsgProtos.ResponseStatus.MULTI_SESSION_WITH_PLAYER);
            }
        }

//        if(session != null && protocol.getId() > 0) {
//            //如果session为null或者id等于0,则为握手请求,可以不校验
//            if(protocol.getId() <= session.getBuilder().getLastId()) {
//                throw new GameProtocolException(GameProtocol.Status.REPEAT_ID, protocol);
//            }
//
//            //校验请求的玩家id要与session绑定的玩家id相同
//            if(session.getBuilder().getPlayerId() > 0) {
//                if(request.getPlayerId() != session.getBuilder().getPlayerId()) {
//                    throw new GameBusinessException(BaseMsgProtos.ResponseStatus.INVALID_PLAYER_IN_SESSION);
//                }
//            }
//            //校验玩家id没有对应其他session
//            if(player != null) {
//                int previousSessionId = player.getProperty().get().getSessionId();
//                if(previousSessionId > 0 && previousSessionId != session.getBuilder().getId()) {
//                    throw new GameBusinessException(BaseMsgProtos.ResponseStatus.INVALID_SESSION_IN_PLAYER);
//                }
//            }
//        }
    }

    public void destroy() {
            dbMediator.close();
    }

    public void initPlayer(int playerId) {
        if(playerId <= 0) {
            throw new GameBusinessException(BaseMsgProtos.ResponseStatus.PLAYER_ID_INVALID);
        }
        this.player = playerDao.load(playerId);
    }

    public Player getPlayer() {
        return player;
    }

    public void savePlayerAfterRequest() {
        if(player == null) return;
        player.getProperty().get().setLastLoginTime(World.now().getTime());
        player.getProperty().markUpdated();
        playerDao.save(player);
    }
}
