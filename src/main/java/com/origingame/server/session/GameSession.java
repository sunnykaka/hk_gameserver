package com.origingame.server.session;

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
}
