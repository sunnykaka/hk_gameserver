package com.origingame.server.protocol;

import com.google.common.base.Preconditions;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.origingame.message.BaseMsgProtos;
import com.origingame.message.HandShakeProtos;
import com.origingame.server.context.GameContext;
import com.origingame.server.exception.GameProtocolException;
import com.origingame.server.session.GameSession;
import com.origingame.server.session.LocalGameSessionMgrImpl;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: liubin
 * Date: 14-3-4
 */
public class RequestWrapper {

    private static final Logger log = LoggerFactory.getLogger(RequestWrapper.class);

    private GameProtocol protocol;

    private BaseMsgProtos.RequestMsg requestMsg;

    private Message message;

    private LocalGameSessionMgrImpl gameSessionMgr = LocalGameSessionMgrImpl.getInstance();

    public RequestWrapper() {}

    public RequestWrapper(GameProtocol protocol) {
        this.protocol = protocol;
        Preconditions.checkArgument(GameProtocol.Type.REQUEST.equals(protocol.getType()));
        byte[] data = protocol.getData();
        if(data == null) return;
        //TODO 不允许data为空
        try {
            this.requestMsg = BaseMsgProtos.RequestMsg.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            log.warn("", e);
            throw new GameProtocolException(GameProtocol.Status.DATA_CORRUPT, protocol);
        }

        //TODO 根据消息类型反序列化消息
//        this.message = ProtocolUtil.parseMessageFromDataAndType(requestMsg.getMessageType(), requestMsg.getMessage().toByteArray());

    }

    public static RequestWrapper parseFromHandShake(GameContext ctx, GameProtocol protocol) {
        RequestWrapper request = new RequestWrapper();
        return request.initRequest(ctx, protocol);
    }

    private RequestWrapper initRequest(GameContext ctx, GameProtocol protocol) {
        Preconditions.checkArgument(GameProtocol.Type.REQUEST.equals(protocol.getType()));
        this.protocol = protocol;
        byte[] data = protocol.getData();
        if(data == null) {
            throw new GameProtocolException(GameProtocol.Status.HANDSHAKE_FAILED, protocol);
        }

        int sessionId = protocol.getSessionId();
        int id = protocol.getId();
        GameSession gameSession = null;
        HandShakeProtos.HandShakeReq handShakeReq;

        if(sessionId > 0) {
            gameSession = gameSessionMgr.load(sessionId);
        }

        try {
            this.requestMsg = BaseMsgProtos.RequestMsg.parseFrom(data);
            byte[] handShakeMessage = getRequestMsg().getMessage().toByteArray();
            handShakeReq = HandShakeProtos.HandShakeReq.parseFrom(handShakeMessage);

        } catch (InvalidProtocolBufferException e) {
            log.warn("", e);
            throw new GameProtocolException(GameProtocol.Status.DATA_CORRUPT, protocol);
        }

        byte[] publicKey = handShakeReq.getPublicKey().toByteArray();
        if(gameSession != null && gameSession.hasPublicKey(publicKey)) {

        } else {
            if(gameSession != null) {
                gameSessionMgr.invalid(gameSession);
            }
            gameSession = gameSessionMgr.init(ctx, publicKey, id);
        }


        return this;
    }

//    public RequestWrapper(int version, GameProtocol.Phase phase, int timeout, Long userId, Message message) {
//        Preconditions.checkNotNull(phase);
//        Preconditions.checkNotNull(message);
//
//        BlasterProtocol.Builder protocol = BlasterProtocol.newBuilder();
//        protocol.setPhase(phase);
//        protocol.setVersion(version);
//        protocol.setTimeout(timeout);
//
//        BaseMsgProtos.RequestMsg.Builder requestMsg = BaseMsgProtos.RequestMsg.newBuilder();
//
//        requestMsg.setMessageType(message.getDescriptorForType().getFullName());
//        requestMsg.setMessage(message.toByteString());
//        if(userId != null) {
//            requestMsg.setUserId(userId);
//        }
//        this.requestMsg = requestMsg.build();
//        protocol.setMessage(this.requestMsg);
//        this.protocol = protocol.build();
//        this.message = message;
//        requestInfo = new RequestInfo(requestMsg.getUserId());
//
//    }

    public BaseMsgProtos.RequestMsg getRequestMsg() {
        return requestMsg;
    }

    public GameProtocol getProtocol() {
        return protocol;
    }

    public Message getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "RequestWrapper{" +
                "protocol=" + protocol +
                ", requestMsg=" + requestMsg +
                ", message=" + message +
                '}';
    }
}
