package com.origingame.server.session;

import com.origingame.config.GlobalConfig;
import com.origingame.server.context.GameContext;
import com.origingame.server.util.IdGenerator;
import com.origingame.server.util.RedisUtil;
import com.origingame.util.World;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.Jedis;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * User: Liub
 * Date: 2014/11/13
 */
public class LocalGameSessionMgrImpl {

    private ConcurrentMap<Integer, GameSession> sessionMap = new ConcurrentHashMap<>();

    private static LocalGameSessionMgrImpl INSTANCE = new LocalGameSessionMgrImpl();

    private LocalGameSessionMgrImpl() {}

    public static LocalGameSessionMgrImpl getInstance() {
        return INSTANCE;
    }

    public GameSession load(int sessionId) {
        GameSession session = sessionMap.get(sessionId);
        if(session.getLastTime() + GlobalConfig.GAME_SESSION_TIMEOUT * 1000 < World.now().getTime()) {
            sessionMap.remove(sessionId);
            return null;
        }

        return session;
    }

    public void invalid(GameSession gameSession) {

        sessionMap.remove(gameSession.getSessionId());

    }

    public void invalid(int previousSessionId) {
        sessionMap.remove(previousSessionId);

    }

    public GameSession init(GameContext ctx, byte[] publicKey) {
        GameSession gameSession = new GameSession();
        gameSession.setPublicKey(publicKey);
        gameSession.setSessionId(IdGenerator.nextSessionId(ctx.getJedis()));
        return gameSession;
    }


    public int getSessionIdByPlayerId(GameContext ctx, int playerId) {

        Jedis jedis = ctx.getJedis();
        String sessionId = jedis.hget(RedisUtil.buildKey("player_id", String.valueOf(playerId)), "session_id");
        return StringUtils.isBlank(sessionId) ? 0 : Integer.valueOf(sessionId);

    }


}
