package com.origingame.server.context;

import com.origingame.server.dao.DbMediator;
import com.origingame.exception.GameProtocolException;
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

    private GameSession session;

    private Channel channel;

    private ServerRequestWrapper request;

    private ServerResponseWrapper response;

    private DbMediator dbMediator;

    public GameContext(Channel channel, GameProtocol protocol) {
        this.channel = channel;
        this.dbMediator = new DbMediator();
        this.request = ServerRequestWrapper.fromProtocol(channel, protocol);
        int sessionId = protocol.getSessionId();
        if(sessionId > 0) {
            this.session = GameSession.load(this,sessionId);
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

    public void checkSession(int id, int playerId, String deviceId) {

        GameProtocol protocol = request.getProtocol();
        if(!protocol.getPhase().equals(GameProtocol.Phase.HAND_SHAKE)) {
            if(session == null) {
                throw new GameProtocolException(GameProtocol.Status.INVALID_SESSION_ID, protocol);
            }
            if(id <= 0) {
                throw new GameProtocolException(GameProtocol.Status.INVALID_ID, protocol);
            }
        }

        if(session != null && id > 0) {
            //如果session为null或者id等于0,则为握手请求,可以不校验
            if(id <= session.getBuilder().getLastId()) {
                throw new GameProtocolException(GameProtocol.Status.REPEAT_ID, protocol);
            }
            if(session.getBuilder().getPlayerId() > 0) {
                if(playerId != session.getBuilder().getPlayerId()) {
                    throw new GameProtocolException(GameProtocol.Status.INVALID_PLAYER_ID_IN_SESSION, protocol);
                }
//                //校验玩家id没有对应其他session
//                int previousSessionId = gameSessionMgr.getSessionIdByPlayerId(this, session.getModel().getPlayerId());
//                if(previousSessionId > 0) {
//                    //使原session失效
//                    GameSession.invalid(this, previousSessionId);
//                }
            }


        }


    }

    public void releaseResources() {
        dbMediator.close();
    }
}
