package com.origingame.client.protocol;

import com.google.common.base.Preconditions;
import com.google.protobuf.Message;
import com.origingame.client.main.ClientSession;
import com.origingame.message.BaseMsgProtos;
import com.origingame.server.protocol.GameProtocol;
import com.origingame.util.crypto.CryptoContext;
import io.netty.channel.Channel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: liubin
 * Date: 14-3-4
 */
public class ClientRequestWrapper {

    private static final Logger log = LoggerFactory.getLogger(ClientRequestWrapper.class);

    //key:sessionId, value:id计数器
    //FIXME 潜在的内存泄漏,只有put没有remove
    private static ConcurrentMap<Integer, AtomicInteger> requestIdCounterMap = new ConcurrentHashMap<>();

    private GameProtocol protocol;

    private BaseMsgProtos.RequestMsg.Builder requestMsg;

    private Channel channel;

    private GameProtocol.Phase phase;

    private Message message;

    private ClientSession clientSession;

    private int requestId;

    private boolean handShake;


    private ClientRequestWrapper() {
    }

    public static ClientRequestWrapper createMessageRequest(Message message, String messageType, ClientSession clientSession, boolean handShake) {

        ClientRequestWrapper request = new ClientRequestWrapper();

        request.clientSession = clientSession;
        request.message = message;
        request.requestId = clientSession.incrementAndGetRequestId();
        request.handShake = handShake;
        request.channel = clientSession.getChannel();

        request.requestMsg = BaseMsgProtos.RequestMsg.newBuilder();

        if(handShake) {
            Preconditions.checkNotNull(clientSession.getPrivateKey());
            Preconditions.checkNotNull(clientSession.getPublicKey());
            request.phase = GameProtocol.Phase.HAND_SHAKE;
            request.requestMsg.setMessage(message.toByteString());
            request.requestMsg.setMessageType(message.getDescriptorForType().getFullName());
        } else {
            Preconditions.checkNotNull(clientSession.getAesPasswordKey());
            Preconditions.checkState(clientSession.getSessionId() > 0);

            request.phase = GameProtocol.Phase.CIPHER_TEXT;

            request.requestMsg.setMessageType(messageType);
            if(message != null) {
                request.requestMsg.setMessage(message.toByteString());
            }

        }
        if(!StringUtils.isBlank(clientSession.getDeviceId())) {
            request.requestMsg.setDeviceId(clientSession.getDeviceId());
        }
        request.requestMsg.setPlayerId(clientSession.getPlayerId());

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
        if(phase.equals(GameProtocol.Phase.HAND_SHAKE)) {

        } else{
            Preconditions.checkNotNull(requestMsg);
            cryptoContext = CryptoContext.createAESCrypto(clientSession.getAesPasswordKey());
        }

        GameProtocol.Builder protocol = GameProtocol.newBuilder();
        protocol.setPhase(phase);
        protocol.setRequestId(requestId);
        protocol.setSessionId(clientSession.getSessionId());
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

    public byte[] getPasswordKey() {
        if(handShake) {
            return clientSession.getPrivateKey();
        } else {
            return clientSession.getAesPasswordKey();
        }
    }

    public GameProtocol.Phase getPhase() {
        return phase;
    }


    @Override
    public String toString() {
        return "RequestWrapper{" +
                "protocol=" + protocol +
                ", requestMsg=" + requestMsg +
                ", message=" + message +
                '}';
    }

    public int getRequestId() {
        return requestId;
    }

//    public int getRequestId() {
//        if(requestId <= 0) {
//            AtomicInteger requestIdCounter = requestIdCounterMap.get(sessionId);
//            if(requestIdCounter == null) {
//                requestIdCounter = new AtomicInteger(0);
//                AtomicInteger newRequestIdCounter = requestIdCounterMap.putIfAbsent(sessionId, requestIdCounter);
//                requestId = newRequestIdCounter == null ? requestIdCounter.incrementAndGet() : newRequestIdCounter.incrementAndGet();
//            }
//        }
//        return requestId;
//    }

}
