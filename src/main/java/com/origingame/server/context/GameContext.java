package com.origingame.server.context;

import com.google.protobuf.Message;
import com.origingame.message.BaseMsgProtos;
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

    private boolean isRequestMessage = false;

    public GameContext(Channel channel) {
        this.channel = channel;
    }

    public GameContext(Channel channel, GameProtocol protocol) {
        this.channel = channel;
        if(GameProtocol.Type.REQUEST.equals(protocol.getType())) {
            isRequestMessage = true;
            this.request = new RequestWrapper(this, protocol);
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
}
