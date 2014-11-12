package com.origingame.server.protocol;

import com.google.common.base.Preconditions;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.origingame.message.BaseMsgProtos;
import com.origingame.server.exception.GameProtocolException;

/**
 * User: liubin
 * Date: 14-3-4
 */
public class RequestWrapper {

    private GameProtocol protocol;

    private BaseMsgProtos.RequestMsg requestMsg;

    private Message message;

    public RequestWrapper(GameProtocol protocol) {
        this.protocol = protocol;
        Preconditions.checkArgument(GameProtocol.Type.REQUEST.equals(protocol.getType()));
        byte[] data = protocol.getData();
        if(data == null) return;
        //TODO 不允许data为空
        try {
            this.requestMsg = BaseMsgProtos.RequestMsg.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            throw new GameProtocolException(GameProtocol.Status.DATA_CORRUPT, protocol);
        }

//        this.message = ProtocolUtil.parseMessageFromDataAndType(requestMsg.getMessageType(), requestMsg.getMessage().toByteArray());

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
