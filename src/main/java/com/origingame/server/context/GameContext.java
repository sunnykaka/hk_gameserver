package com.origingame.server.context;

import com.google.protobuf.Message;
import com.origingame.message.BaseMsgProtos;
import com.origingame.server.exception.GameProtocolException;
import com.origingame.server.protocol.GameProtocol;
import com.origingame.server.protocol.RequestWrapper;
import com.origingame.server.protocol.ResponseWrapper;
import com.origingame.server.session.GameSession;
import com.origingame.server.session.LocalGameSessionMgrImpl;
import com.origingame.util.World;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

/**
 * User: Liub
 * Date: 2014/11/13
 */
public class GameContext {

    private static final Logger log = LoggerFactory.getLogger(GameContext.class);

    private GameSession session;

    private Channel channel;

    private LocalGameSessionMgrImpl gameSessionMgr = LocalGameSessionMgrImpl.getInstance();

    private RequestWrapper request;

    private ResponseWrapper response;

    private Jedis jedis;

    public GameContext(Channel channel, GameProtocol protocol) {
        this.channel = channel;
        if(GameProtocol.Type.REQUEST.equals(protocol.getType())) {
            this.request = new RequestWrapper(protocol);
            int sessionId = protocol.getSessionId();
            if(sessionId > 0) {
                this.session = gameSessionMgr.load(sessionId);
            }
        } else {

        }
    }

    public void setSession(GameSession session) {
        this.session = session;
    }

    public void setRequest(RequestWrapper request) {
        this.request = request;
    }

    public void setResponse(ResponseWrapper response) {
        this.response = response;
    }

    public GameSession getSession() {
        return session;
    }

    public Channel getChannel() {
        return channel;
    }

    public RequestWrapper getRequest() {
        return request;
    }

    public ResponseWrapper getResponse() {
        return response;
    }

    public Jedis getJedis() {
        if(jedis == null) {
            jedis = World.getConnection();
        }
        return jedis;
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
            if(id <= session.getLastId()) {
                throw new GameProtocolException(GameProtocol.Status.REPEAT_ID, protocol);
            }
            if(session.getPlayerId() > 0) {
                if(playerId != session.getPlayerId()) {
                    throw new GameProtocolException(GameProtocol.Status.INVALID_PLAYER_ID_IN_SESSION, protocol);
                }
                //校验玩家id没有对应其他session
                int previousSessionId = gameSessionMgr.getSessionIdByPlayerId(this, session.getPlayerId());
                if(previousSessionId > 0) {
                    //使session失效
                    gameSessionMgr.invalid(previousSessionId);
                }
            }


        }


    }
}
