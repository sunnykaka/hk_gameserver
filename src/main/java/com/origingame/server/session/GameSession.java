package com.origingame.server.session;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.origingame.business.player.model.Player;
import com.origingame.config.GlobalConfig;
import com.origingame.server.context.GameContext;
import com.origingame.exception.GameDaoException;
import com.origingame.persist.GameSessionProtos;
import com.origingame.server.util.IdGenerator;
import com.origingame.server.util.RedisUtil;
import com.origingame.util.crypto.AES;
import org.apache.commons.lang3.StringUtils;
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

//    //是否要为创建玩家ID的index
//    private boolean needBindPlayer;
//
//    //该session之前对应的玩家ID
//    private int previousPlayerId;

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
        Jedis jedis = ctx.getDbMediator().selectPlayerDb(gameSessionBuilder.getId()).getJedis();
        jedis.del(buildStoreKey(gameSessionBuilder.getId()));
    }

    public void save() {
        Jedis jedis = ctx.getDbMediator().selectPlayerDb(gameSessionBuilder.getId()).getJedis();
        byte[] key = buildStoreKey(gameSessionBuilder.getId());
        RedisUtil.checkSetResponse(jedis.set(key, gameSessionBuilder.build().toByteArray()));
        jedis.expire(key, GlobalConfig.GAME_SESSION_TIMEOUT_IN_SECONDS);

//        if(needBindPlayer) {
//            String playerIdIndexKey = RedisUtil.buildKey("i", GameSessionProtos.GameSessionModel.class.getSimpleName(), "playerId");
//            Jedis indexJedis = ctx.getDbMediator().selectCenterDb().getJedis();
//
//            if(previousPlayerId > 0) {
//
//            }
//
//            //创建playerId-sessionId的index
//            String previousSessionId = indexJedis.hget(playerIdIndexKey, String.valueOf(gameSessionBuilder.getPlayerId()));
//            //保存index
//            indexJedis.hset(playerIdIndexKey, String.valueOf(gameSessionBuilder.getPlayerId()), String.valueOf(gameSessionBuilder.getId()));
//            //如果之前的session还存在,需要删掉
//            if(!StringUtils.isBlank(previousSessionId) && Integer.parseInt(previousSessionId) != gameSessionBuilder.getId()) {
//                GameSession previousGameSession = load(ctx, Integer.parseInt(previousSessionId));
//                if(previousGameSession != null) {
//                    previousGameSession.invalid();
//                }
//            }
//
//        }
    }

    /**
     * @param playerId
     */
    public void bindPlayer(int playerId) {
//        this.previousPlayerId = this.gameSessionBuilder.getPlayerId();
//        if(previousPlayerId > 0) {
//            if(previousPlayerId == playerId) {
//                //同一用户在同一session重复登录,不用维护index
//                return;
//            }
//        }
//        this.needBindPlayer = true;
        this.gameSessionBuilder.setPlayerId(playerId);
    }
}
