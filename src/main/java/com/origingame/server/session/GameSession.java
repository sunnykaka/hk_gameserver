package com.origingame.server.session;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.origingame.config.GlobalConfig;
import com.origingame.server.context.GameContext;
import com.origingame.exception.GameDaoException;
import com.origingame.persist.GameSessionProtos;
import com.origingame.server.main.World;
import com.origingame.server.util.IdGenerator;
import com.origingame.server.util.RedisUtil;
import com.origingame.util.crypto.AES;
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
        Jedis jedis = ctx.getDbMediator().selectShardDb(sessionId).getJedis();
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
        gameSession.getBuilder().setId(IdGenerator.nextId(ctx.getDbMediator(), GameSessionProtos.GameSessionModel.class));
        gameSession.getBuilder().setPublicKey(publicKey);
        byte[] passwordKey = AES.initPasswordKey();
        gameSession.getBuilder().setPasswordKey(ByteString.copyFrom(passwordKey));

        return gameSession;

    }

    private static byte[] buildStoreKey(int sessionId) {
        return RedisUtil.buildByteKey("session", String.valueOf(sessionId));
    }

    public GameSessionProtos.GameSessionModel.Builder getBuilder() {
        return gameSessionBuilder;
    }


    public boolean hasPublicKey(ByteString publicKey) {
        return RedisUtil.byteStringEquals(getBuilder().getPublicKey(), publicKey);
    }

    public void invalid() {
        Jedis jedis = ctx.getDbMediator().selectShardDb(gameSessionBuilder.getId()).getJedis();
        jedis.del(buildStoreKey(gameSessionBuilder.getId()));
    }

    public void save0() {
        Jedis jedis = ctx.getDbMediator().selectShardDb(gameSessionBuilder.getId()).getJedis();
        byte[] key = buildStoreKey(gameSessionBuilder.getId());
        RedisUtil.checkSetResponse(jedis.set(key, gameSessionBuilder.build().toByteArray()));
        jedis.expire(key, GlobalConfig.GAME_SESSION_TIMEOUT_IN_SECONDS);

    }

    public void save() {
        if (gameSessionBuilder != null) {
            gameSessionBuilder.setLastTime(World.now().getTime());
            int id = ctx.getRequest().getProtocol().getId();
            if (id > 0) {
                gameSessionBuilder.setLastId(id);
            }
            save0();
        }
    }

    /**
     * @param playerId
     */
    public void bindPlayer(int playerId) {
        this.gameSessionBuilder.setPlayerId(playerId);
    }
}
