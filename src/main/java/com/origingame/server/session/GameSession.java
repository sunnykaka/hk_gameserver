package com.origingame.server.session;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.origingame.server.context.GameContext;
import com.origingame.server.exception.GameDaoException;
import com.origingame.server.model.GameSessionProtos;
import com.origingame.server.util.IdGenerator;
import com.origingame.server.util.RedisUtil;
import redis.clients.jedis.Jedis;

/**
 * User: Liub
 * Date: 2014/11/13
 */
public class GameSession {

    private GameContext ctx;

    private GameSessionProtos.GameSessionModel.Builder gameSessionBuilder;

    private GameSession(GameContext ctx) {
        this.ctx = ctx;
    }

    public static GameSession load(GameContext ctx, int sessionId) {
        GameSession gameSession = new GameSession(ctx);
        Jedis jedis = ctx.getDbMediator().selectPlayerDb(sessionId).getJedis();
        byte[] value = jedis.get(buildStoreKey(sessionId));
        if(value == null) return null;
        try {
            gameSession.gameSessionBuilder = GameSessionProtos.GameSessionModel.newBuilder().mergeFrom(value);
        } catch (InvalidProtocolBufferException e) {
            throw new GameDaoException(e);
        }
        return gameSession;
    }

    public static GameSession create(GameContext ctx, ByteString publicKey) {
        GameSession gameSession = new GameSession(ctx);
        gameSession.gameSessionBuilder = GameSessionProtos.GameSessionModel.newBuilder();
        gameSession.getBuilder().setId(IdGenerator.nextSessionId(ctx.getDbMediator().selectCenterDb().getJedis()));
        gameSession.getBuilder().setPublicKey(publicKey);
        return gameSession;

    }


    private static byte[] buildStoreKey(int sessionId) {
        return RedisUtil.buildByteKey("session", String.valueOf(sessionId));
    }

    public GameSessionProtos.GameSessionModel.Builder getBuilder() {
        return gameSessionBuilder;
    }


    public static void invalid(GameContext ctx, int previousSessionId) {
        Jedis jedis = ctx.getDbMediator().selectPlayerDb(previousSessionId).getJedis();
        //直接删除session
        jedis.del(buildStoreKey(previousSessionId));
    }

    public boolean hasPublicKey(ByteString publicKey) {
        return RedisUtil.byteStringEquals(getBuilder().getPublicKey(), publicKey);
    }

    public void invalid() {
        Jedis jedis = ctx.getDbMediator().selectPlayerDb(gameSessionBuilder.getId()).getJedis();
        jedis.del(buildStoreKey(gameSessionBuilder.getId()));
    }

    public void save() {
        Jedis jedis = ctx.getDbMediator().selectPlayerDb(gameSessionBuilder.getId()).getJedis();
        RedisUtil.checkSetResponse(jedis.set(buildStoreKey(gameSessionBuilder.getId()), gameSessionBuilder.build().toByteArray()));
    }
}
