package com.origingame.server.session;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.origingame.config.GlobalConfig;
import com.origingame.server.context.GameContext;
import com.origingame.server.exception.GameDaoException;
import com.origingame.server.model.GameSessionProtos;
import com.origingame.server.protocol.GameProtocol;
import com.origingame.server.util.IdGenerator;
import com.origingame.server.util.RedisUtil;
import com.origingame.util.World;
import redis.clients.jedis.Jedis;

import java.util.Arrays;

/**
 * User: Liub
 * Date: 2014/11/13
 */
public class GameSession {

    private GameContext ctx;

    private GameSessionProtos.GameSessionModel.Builder gameSessionBuilder;

//    private byte[] passwordKey;
//
//    private byte[] publicKey;

    private GameSession(GameContext ctx) {
        this.ctx = ctx;
    }

    public static GameSession load(GameContext ctx, int sessionId) {
        GameSession gameSession = new GameSession(ctx);
        Jedis jedis = ctx.getJedis();
        byte[] value = jedis.get(buildStoreKey(sessionId));
        if(value == null) return null;
        try {
            gameSession.gameSessionBuilder = GameSessionProtos.GameSessionModel.newBuilder().mergeFrom(value);
        } catch (InvalidProtocolBufferException e) {
            throw new GameDaoException(e);
        }
        return gameSession;
//        GameSession session = sessionMap.get(sessionId);
//        if(session.getLastTime() + GlobalConfig.GAME_SESSION_TIMEOUT * 1000 < World.now().getTime()) {
//            sessionMap.remove(sessionId);
//            return null;
//        }
    }

    public static GameSession create(GameContext ctx, ByteString publicKey) {
        GameSession gameSession = new GameSession(ctx);
        gameSession.gameSessionBuilder = GameSessionProtos.GameSessionModel.newBuilder();
        gameSession.getBuilder().setId(IdGenerator.nextSessionId(ctx.getJedis()));
        gameSession.getBuilder().setPublicKey(publicKey);
        return gameSession;

    }


    private static byte[] buildStoreKey(int sessionId) {
        return RedisUtil.buildByteKey("session", String.valueOf(sessionId));
    }
//    private static String buildSessionInvalidKey(int previousSessionId) {
//        return RedisUtil.buildKey("session", "invalid", String.valueOf(previousSessionId);
//    }

    public GameSessionProtos.GameSessionModel.Builder getBuilder() {
        return gameSessionBuilder;
    }

    //reduce array copy times
//    public byte[] getPasswordKey() {
//        if(passwordKey == null) {
//            this.passwordKey = getBuilder().getPasswordKey().toByteArray();
//        }
//        return passwordKey;
//    }
//
//    public byte[] getPublicKey() {
//        if(publicKey == null) {
//            this.publicKey = getBuilder().getPublicKey().toByteArray();
//        }
//        return publicKey;
//    }

//    private void checkIfInvalid(int sessionId) {
//        String result = ctx.getJedis().get(buildSessionInvalidKey(sessionId));
//        if(result != null) {
//
//        }
//    }

    public static void invalid(GameContext ctx, int previousSessionId) {
        Jedis jedis = ctx.getJedis();
        //900秒后超时
//        RedisUtil.checkSetResponse(jedis.set(buildSessionInvalidKey(previousSessionId), "1", "NX", "EX", GlobalConfig.GAME_SESSION_TIMEOUT));
        //直接删除session
        jedis.del(buildStoreKey(previousSessionId));
    }

    public boolean hasPublicKey(ByteString publicKey) {
        return RedisUtil.byteStringEquals(getBuilder().getPublicKey(), publicKey);
    }

    public void invalid() {
        Jedis jedis = ctx.getJedis();
        jedis.del(buildStoreKey(gameSessionBuilder.getId()));
    }

    public void save() {
        Jedis jedis = ctx.getJedis();
        RedisUtil.checkSetResponse(jedis.set(buildStoreKey(gameSessionBuilder.getId()), gameSessionBuilder.build().toByteArray()));
    }
}
