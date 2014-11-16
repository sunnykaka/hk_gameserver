package com.origingame.server.session;

import com.origingame.config.GlobalConfig;
import com.origingame.server.context.GameContext;
import com.origingame.server.util.RedisUtil;
import com.origingame.util.World;
import redis.clients.jedis.Jedis;

import java.util.Arrays;

/**
 * User: Liub
 * Date: 2014/11/13
 */
public class GameSession {

    private int sessionId;

    private byte[] passwordKey;

    private byte[] publicKey;

    private int playerId;

    private long lastTime;

    private int lastId;

    private String deviceId;

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public byte[] getPasswordKey() {
        return passwordKey;
    }

    public void setPasswordKey(byte[] passwordKey) {
        this.passwordKey = passwordKey;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public long getLastTime() {
        return lastTime;
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    public int getLastId() {
        return lastId;
    }

    public void setLastId(int lastId) {
        this.lastId = lastId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public boolean hasPublicKey(byte[] publicKey) {
        return Arrays.equals(this.publicKey, publicKey);
    }

    public static GameSession load(GameContext ctx, int sessionId) {
        Jedis jedis = ctx.getJedis();


        jedis.hgetAll(RedisUtil.buildKey("session", String.valueOf(sessionId)));

//        GameSession session = sessionMap.get(sessionId);
//        if(session.getLastTime() + GlobalConfig.GAME_SESSION_TIMEOUT * 1000 < World.now().getTime()) {
//            sessionMap.remove(sessionId);
//            return null;
//        }

        return null;
    }
}
