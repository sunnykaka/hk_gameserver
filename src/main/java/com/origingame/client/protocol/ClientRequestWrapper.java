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

/**
 * User: liubin
 * Date: 14-3-4
 */
public class ClientRequestWrapper {

    private static final Logger log = LoggerFactory.getLogger(ClientRequestWrapper.class);

    //key:sessionId, value:id计数器
//    //FIXME 潜在的内存泄漏,只有put没有remove
//    private static ConcurrentMap<Integer, AtomicInteger> requestIdCounterMap = new ConcurrentHashMap<>();

    private GameProtocol protocol;

    private BaseMsgProtos.RequestMsg.Builder requestMsg;

    private Channel channel;

    private GameProtocol.Phase phase;

    private String messageType;

    private Message message;

    private ClientSession clientSession;

    private int requestId;

    private boolean shakeHand;


    private void init() {
        this.requestId = clientSession.incrementAndGetRequestId();
        this.channel = clientSession.getChannel();

        this.requestMsg = BaseMsgProtos.RequestMsg.newBuilder();

        if(shakeHand) {
            Preconditions.checkNotNull(clientSession.getPrivateKey());
            Preconditions.checkNotNull(clientSession.getPublicKey());
            this.phase = GameProtocol.Phase.HAND_SHAKE;
            this.requestMsg.setMessage(message.toByteString());
            this.requestMsg.setMessageType(message.getDescriptorForType().getFullName());
        } else {
            Preconditions.checkNotNull(clientSession.getAesPasswordKey());
            Preconditions.checkState(clientSession.getSessionId() > 0);

            this.phase = GameProtocol.Phase.CIPHER_TEXT;

            this.requestMsg.setMessageType(messageType);
            if(message != null) {
                this.requestMsg.setMessage(message.toByteString());
            }

        }
        if(!StringUtils.isBlank(clientSession.getDeviceId())) {
            this.requestMsg.setDeviceId(clientSession.getDeviceId());
        }
        this.requestMsg.setPlayerId(clientSession.getPlayerId());
    }

    public static ClientRequestWrapper createMessageRequest(Message message, String messageType,
                                                            ClientSession clientSession, boolean shakeHand) {

        ClientRequestWrapper request = new ClientRequestWrapper();

        request.clientSession = clientSession;
        request.message = message;
        request.shakeHand = shakeHand;
        request.messageType = messageType;

        request.init();
        request.buildProtocol();

        return request;

    }

    public GameProtocol getProtocol() {
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
        return messageType;
    }

    public int getPlayerId() {
        return requestMsg.getPlayerId();
    }

    public String getDeviceId() {
        return requestMsg.getDeviceId();
    }

    public byte[] getPasswordKey() {
        if(shakeHand) {
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
        return "ClientRequestWrapper{" +
                "protocol=" + protocol +
                ", requestMsg=" + requestMsg +
                ", channel=" + channel +
                ", phase=" + phase +
                ", messageType='" + messageType + '\'' +
                ", message=" + message +
                ", requestId=" + requestId +
                ", shakeHand=" + shakeHand +
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
