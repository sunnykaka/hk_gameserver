package com.origingame.client.main;

import com.google.common.base.Preconditions;
import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import com.origingame.client.protocol.ClientRequestWrapper;
import com.origingame.client.protocol.ClientResponseWrapper;
import com.origingame.exception.CryptoException;
import com.origingame.exception.GameClientException;
import com.origingame.message.HandShakeProtos;
import com.origingame.server.message.MessageDispatcher;
import com.origingame.util.crypto.RSA;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by liubin on 14/11/22.
 */
public class ClientSession {

    private static final Logger log = LoggerFactory.getLogger(ClientSession.class);

    private int sessionId;

    private int requestId;

    private NettyGameClient nettyGameClient;

    private byte[] publicKey;

    private byte[] privateKey;

    private byte[] aesPasswordKey;

    private boolean shakeHandSuccess;

    private boolean connected;

    private String host;

    private int port;

    private int playerId;

    private String deviceId;


    public ClientSession(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void openConnection() {
        closeConnection();

        try {

            nettyGameClient = new NettyGameClient(host, port);
            nettyGameClient.start();
            connected = true;

        } catch (IOException | InterruptedException e) {
            log.error("", e);
            throw new GameClientException("连接服务器失败", e);
        }
    }

    public void closeConnection() {
        if(nettyGameClient != null) {
            try {
                nettyGameClient.stop();
            } catch (IOException | InterruptedException e) {
                log.error("关闭之前的连接失败", e);
            }
            nettyGameClient = null;
        }
        connected = false;
    }

    public void shakeHand() {

        checkConnected();

        try {

            log.info("握手请求,当前sessionId[{}], 当前握手状态[{}]", sessionId, shakeHandSuccess);

            Object[] keys = RSA.initKey();
            this.publicKey = (byte[])keys[0];
            this.privateKey = (byte[])keys[1];

            HandShakeProtos.HandShakeReq.Builder handShakeReq = HandShakeProtos.HandShakeReq.newBuilder();
            handShakeReq.setPublicKey(ByteString.copyFrom(publicKey));
            ClientRequestWrapper requestWrapper = ClientRequestWrapper.createMessageRequest(handShakeReq.build(),
                    handShakeReq.getDescriptorForType().getFullName(), this, true);

            ClientResponseWrapper response = MessageDispatcher.getInstance().request(requestWrapper);
            if(!response.isSuccess()) {
                throw new GameClientException("握手失败!, response:" + response);
            }
            HandShakeProtos.HandShakeResp handShakeResp = (HandShakeProtos.HandShakeResp) response.getMessage();
            sessionId = handShakeResp.getSessionId();
            aesPasswordKey = handShakeResp.getPasswordKey().toByteArray();
            Preconditions.checkArgument(sessionId > 0);
            Preconditions.checkNotNull(aesPasswordKey);
            shakeHandSuccess = true;

            log.info("握手成功, sessionId[{}]", sessionId);

        } catch (CryptoException e) {
            log.error("", e);
            shakeHandSuccess = false;
            throw new GameClientException("握手失败", e);
        }

    }


    public ClientResponseWrapper sendMessage(Message message) {
        Preconditions.checkNotNull(message);
        return sendMessage0(message, message.getDescriptorForType().getFullName());
    }

    public ClientResponseWrapper sendEmptyMessage(String messageType) {
        Preconditions.checkNotNull(messageType);
        return sendMessage0(null, messageType);
    }

    private ClientResponseWrapper sendMessage0(Message message, String messageType) {
        checkConnected();
        checkShakeHanded();

        ClientRequestWrapper request = ClientRequestWrapper.createMessageRequest(message, messageType, this, false);
        log.info("发送请求: {}", request);
        ClientResponseWrapper response = MessageDispatcher.getInstance().request(request);
        log.info("接收响应: {}", response);
        if(!response.isSuccess()) {
            throw new GameClientException("请求失败!, response:" + response);
        }
        return response;

    }

    private void checkShakeHanded() {
        if(!shakeHandSuccess) {
            throw new GameClientException("发送数据之前需要先进行握手");
        }
    }

    private void checkConnected() {
        if(!connected) {
            throw new GameClientException("握手之前需要先连接服务器");
        }
    }


    public void destroy() {
        closeConnection();
        publicKey = null;
        privateKey = null;
        aesPasswordKey = null;
        sessionId = 0;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public int getSessionId() {
        return sessionId;
    }

    public byte[] getPrivateKey() {
        return privateKey;
    }

    public byte[] getAesPasswordKey() {
        return aesPasswordKey;
    }

    public boolean isShakeHandSuccess() {
        return shakeHandSuccess;
    }

    public boolean isConnected() {
        return connected;
    }

    public int incrementAndGetRequestId() {
        return ++requestId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public int getPlayerId() {
        return playerId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public Channel getChannel() {
        return nettyGameClient.getChannel();
    }


    @Override
    public String toString() {
        return "ClientSession{" +
                "sessionId=" + sessionId +
                ", requestId=" + requestId +
                ", shakeHandSuccess=" + shakeHandSuccess +
                ", connected=" + connected +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", playerId=" + playerId +
                ", deviceId='" + deviceId + '\'' +
                '}';
    }
}
