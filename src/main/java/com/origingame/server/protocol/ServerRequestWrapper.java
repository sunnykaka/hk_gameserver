package com.origingame.server.protocol;

import com.google.common.base.Preconditions;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.origingame.message.BaseMsgProtos;
import com.origingame.message.HandShakeProtos;
import com.origingame.server.exception.CryptoException;
import com.origingame.server.exception.GameProtocolException;
import com.origingame.util.crypto.CryptoContext;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: liubin
 * Date: 14-3-4
 */
public class ServerRequestWrapper {

    private static final Logger log = LoggerFactory.getLogger(ServerRequestWrapper.class);

    private GameProtocol protocol;

    private BaseMsgProtos.RequestMsg requestMsg;

    private BaseMsgProtos.RequestMsg.Builder requestMsgBuilder;

    private Message message;

    private Channel channel;

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

    public static ServerRequestWrapper fromProtocol(Channel channel, GameProtocol protocol) {

        Preconditions.checkArgument(GameProtocol.Type.REQUEST.equals(protocol.getType()));
        byte[] data = protocol.getData();
        if(data == null || data.length == 0) {
            throw new GameProtocolException(GameProtocol.Status.REQUEST_MESSAGE_EMPTY, protocol);
        }

        ServerRequestWrapper request = new ServerRequestWrapper();
        request.protocol = protocol;
        request.channel = channel;

        return request;
    }

    public static ServerRequestWrapper fromMessage(Message message) {
        ServerRequestWrapper request = new ServerRequestWrapper();
        request.requestMsgBuilder = BaseMsgProtos.RequestMsg.newBuilder();
        if(message != null) {
            request.requestMsgBuilder.setMessage(message.toByteString());
            request.requestMsgBuilder.setMessageType(message.getDescriptorForType().getFullName());
        }
        return request;
    }

//    public static RequestWrapper parseFromHandShake(GameContext ctx, GameProtocol protocol) {
//        RequestWrapper request = new RequestWrapper();
//        return request.initRequest(ctx, protocol);
//    }

    public ServerRequestWrapper init() {


//        switch (protocol.getPhase()) {
//            case HAND_SHAKE: {
//                HandShakeProtos.HandShakeReq handShakeReq;
//
//                try {
//                    this.requestMsg = BaseMsgProtos.RequestMsg.parseFrom(data);
//                    byte[] handShakeMessage = getRequestMsg().getMessage().toByteArray();
//                    handShakeReq = HandShakeProtos.HandShakeReq.parseFrom(handShakeMessage);
//                    this.message = handShakeReq;
//
//                } catch (InvalidProtocolBufferException e) {
//                    log.warn("", e);
//                    throw new GameProtocolException(GameProtocol.Status.DATA_CORRUPT, protocol);
//                }
//
//                break;
//            }
//            case PLAIN_TEXT: {
//                break;
//            }
//            case CIPHER_TEXT: {
//
//
//
//                break;
//            }
//            default:
//                throw new GameProtocolException(GameProtocol.Status.INVALID_PHASE, protocol);
//        }

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

    public Channel getChannel() {
        return channel;
    }

    public String getMessageType() {
        if(requestMsg != null) {
            return requestMsg.getMessageType();
        } else if(requestMsgBuilder != null) {
            return requestMsgBuilder.getMessageType();
        } else {
            return null;
        }
    }

    public int getPlayerId() {
        if(requestMsg != null) {
            return requestMsg.getPlayerId();
        } else if(requestMsgBuilder != null) {
            return requestMsgBuilder.getPlayerId();
        } else {
            return 0;
        }
    }

    public String getDeviceId() {
        if(requestMsg != null) {
            return requestMsg.getDeviceId();
        } else if(requestMsgBuilder != null) {
            return requestMsgBuilder.getDeviceId();
        } else {
            return null;
        }
    }


    public ServerRequestWrapper setPlayerId(int playerId) {
        requestMsgBuilder.setPlayerId(playerId);
        return this;
    }

    public ServerRequestWrapper setDeviceId(String deviceId) {
        requestMsgBuilder.setDeviceId(deviceId);
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

    public void parseHandShakeMessage() {

        try {
            this.requestMsg = BaseMsgProtos.RequestMsg.parseFrom(protocol.getData());
            byte[] handShakeMessage = requestMsg.getMessage().toByteArray();
            this.message = HandShakeProtos.HandShakeReq.parseFrom(handShakeMessage);

        } catch (InvalidProtocolBufferException e) {
            log.warn("", e);
            throw new GameProtocolException(GameProtocol.Status.DATA_CORRUPT, protocol);
        }

    }

    public void parseCipherMessage(CryptoContext cryptoContext) {

        try {
            byte[] data = protocol.getData();
            data = cryptoContext.decrypt(data);
            this.requestMsg = BaseMsgProtos.RequestMsg.parseFrom(data);
            this.message = ProtocolUtil.parseMessageFromDataAndType(requestMsg.getMessageType(), requestMsg.getMessage().toByteArray());

        } catch (CryptoException e) {
            log.warn("", e);
            throw new GameProtocolException(GameProtocol.Status.DECIPHER_FAILED, protocol);
        } catch (InvalidProtocolBufferException e) {
            log.warn("", e);
            throw new GameProtocolException(GameProtocol.Status.DATA_CORRUPT, protocol);
        }



    }
}
