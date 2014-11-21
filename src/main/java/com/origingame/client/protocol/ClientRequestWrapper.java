package com.origingame.client.protocol;

import com.google.common.base.Preconditions;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.origingame.message.BaseMsgProtos;
import com.origingame.message.HandShakeProtos;
import com.origingame.server.exception.CryptoException;
import com.origingame.server.exception.GameProtocolException;
import com.origingame.server.protocol.GameProtocol;
import com.origingame.server.protocol.ProtocolUtil;
import com.origingame.util.crypto.CryptoContext;
import io.netty.channel.Channel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: liubin
 * Date: 14-3-4
 */
public class ClientRequestWrapper {

    private static final Logger log = LoggerFactory.getLogger(ClientRequestWrapper.class);

    private static AtomicInteger requestIdCounter = new AtomicInteger(0);

    private GameProtocol protocol;

    private BaseMsgProtos.RequestMsg.Builder requestMsg;

    private Channel channel;

    private GameProtocol.Phase phase;

    private byte[] passwordKey;

    private byte[] publicKey;

    private int requestId;

    private int sessionId;



    public ClientRequestWrapper() {
        this.requestId = requestIdCounter.incrementAndGet();
    }

    //    public RequestWrapper(GameProtocol protocol) {
//        this.protocol = protocol;
//        Preconditions.checkArgument(GameProtocol.Type.REQUEST.equals(protocol.getType()));
//        byte[] data = protocol.getData();
//        if(data == null) return;
//        try {
//            this.requestMsg = BaseMsgProtos.RequestMsg.parseFrom(data);
//        } catch (InvalidProtocolBufferException e) {
//            log.warn("", e);
//            throw new GameProtocolException(GameProtocol.Status.DATA_CORRUPT, protocol);
//        }
//
//        this.message = ProtocolUtil.parseMessageFromDataAndType(requestMsg.getMessageType(), requestMsg.getMessage().toByteArray());
//
//    }

    public static ClientRequestWrapper fromBusinessMessage(Message message, int sessionId, byte[] passwordKey) {

        return fromMessage(message, sessionId, passwordKey, false);
    }

    public static ClientRequestWrapper fromHandShake(Message message, int sessionId) {

        return fromMessage(message, sessionId, null, true);
    }


    private static ClientRequestWrapper fromMessage(Message message, int sessionId, byte[] passwordKey, boolean handShake) {


        ClientRequestWrapper request = new ClientRequestWrapper();
        request.requestMsg = BaseMsgProtos.RequestMsg.newBuilder();
        request.sessionId = sessionId;

        if(handShake) {
            request.phase = GameProtocol.Phase.HAND_SHAKE;
            request.requestMsg.setMessage(message.toByteString());
            request.requestMsg.setMessageType(message.getDescriptorForType().getFullName());
        } else {
            Preconditions.checkState(sessionId > 0);
            Preconditions.checkNotNull(passwordKey);
            request.phase = GameProtocol.Phase.CIPHER_TEXT;
            request.passwordKey = passwordKey;
            if(message != null) {
                request.requestMsg.setMessage(message.toByteString());
                request.requestMsg.setMessageType(message.getDescriptorForType().getFullName());
            }
        }

        return request;

    }

    public GameProtocol getProtocol() {
        if(protocol == null) {
            buildProtocol();
        }
        return protocol;
    }

    private void buildProtocol() {
        Preconditions.checkNotNull(phase);
        CryptoContext cryptoContext = null;
        if(passwordKey != null) {
            cryptoContext = CryptoContext.createAESCrypto(passwordKey);
        }
        if(phase.equals(GameProtocol.Phase.HAND_SHAKE)) {

        } else{
            Preconditions.checkNotNull(requestMsg);
            cryptoContext = CryptoContext.createAESCrypto(passwordKey);
            requestMsg.setMessageType(message.getDescriptorForType().getFullName());
            requestMsg.setMessage(message.toByteString());
        }

        GameProtocol.Builder protocol = GameProtocol.newBuilder();
        protocol.setPhase(phase);
        protocol.setRequestId(requestId);
        protocol.setSessionId(sessionId);
        protocol.setMessage(requestMsg.build(), cryptoContext);
        this.protocol = protocol.build();
    }

    public Message getMessage() {
        return message;
    }

    public Channel getChannel() {
        return channel;
    }

    public String getMessageType() {
        return requestMsg.getMessageType();
    }

    public int getPlayerId() {
        return requestMsg.getPlayerId();
    }

    public String getDeviceId() {
        return requestMsg.getDeviceId();
    }


    public ClientRequestWrapper setPlayerId(int playerId) {
        requestMsg.setPlayerId(playerId);
        return this;
    }

    public ClientRequestWrapper setDeviceId(String deviceId) {
        requestMsg.setDeviceId(deviceId);
        return this;
    }




    @Override
    public String toString() {
        return "RequestWrapper{" +
                "protocol=" + protocol +
                ", requestMsg=" + requestMsg +
                ", message=" + message +
                '}';
    }

//    public void parseHandShakeMessage() {
//
//        try {
//            this.requestMsg = BaseMsgProtos.RequestMsg.parseFrom(protocol.getData());
//            byte[] handShakeMessage = requestMsg.getMessage().toByteArray();
//            this.message = HandShakeProtos.HandShakeReq.parseFrom(handShakeMessage);
//
//        } catch (InvalidProtocolBufferException e) {
//            log.warn("", e);
//            throw new GameProtocolException(GameProtocol.Status.DATA_CORRUPT, protocol);
//        }
//
//    }
//
//    public void parseCipherMessage(CryptoContext cryptoContext) {
//
//        try {
//            byte[] data = protocol.getData();
//            data = cryptoContext.decrypt(data);
//            this.requestMsg = BaseMsgProtos.RequestMsg.parseFrom(data);
//            this.message = ProtocolUtil.parseMessageFromDataAndType(requestMsg.getMessageType(), requestMsg.getMessage().toByteArray());
//
//        } catch (CryptoException e) {
//            log.warn("", e);
//            throw new GameProtocolException(GameProtocol.Status.DECIPHER_FAILED, protocol);
//        } catch (InvalidProtocolBufferException e) {
//            log.warn("", e);
//            throw new GameProtocolException(GameProtocol.Status.DATA_CORRUPT, protocol);
//        }
//
//
//
//    }
}
