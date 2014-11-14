package com.origingame.server.protocol;

import com.google.common.base.Preconditions;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.origingame.message.BaseMsgProtos;
import com.origingame.server.context.GameContext;
import com.origingame.server.exception.GameProtocolException;
import com.origingame.util.crypto.CryptoContext;

/**
 * User: liubin
 * Date: 14-3-4
 */
public class ResponseWrapper {

    private GameProtocol protocol;

    private BaseMsgProtos.ResponseMsg responseMsg;

    private Message message;

    public ResponseWrapper() {}

    public ResponseWrapper(GameProtocol protocol) {
        this.protocol = protocol;
        Preconditions.checkArgument(GameProtocol.Type.RESPONSE.equals(protocol.getType()));
        byte[] data = protocol.getData();
        if(data == null) return;
        try {
            this.responseMsg = BaseMsgProtos.ResponseMsg.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            throw new GameProtocolException(GameProtocol.Status.DATA_CORRUPT, protocol);
        }
        if(responseMsg.getMessageType() != null && responseMsg.getMessage() != null) {
//            this.message = ProtocolUtil.parseMessageFromDataAndType(responseMsg.getMessageType(), responseMsg.getMessage().toByteArray());
        }

    }
//
//    public ResponseWrapper(GameProtocol.Phase phase, int id, GameProtocol.Status status, BaseMsgProtos.ResponseStatus responseStatus,
//                           String msg, Message message) {
//        Preconditions.checkNotNull(phase);
//        Preconditions.checkNotNull(status);
//
//        GameProtocol.Builder protocol = GameProtocol.newBuilder();
//        protocol.setPhase(phase);
//        protocol.setStatus(status);
//        protocol.setResponseId(id);
//
//        if(GameProtocol.Status.SUCCESS.equals(status)) {
//            //只有正常解析协议了才设置业务对象
//            BaseMsgProtos.ResponseMsg.Builder responseMsg = BaseMsgProtos.ResponseMsg.newBuilder();
//            if(responseStatus != null) {
//                responseMsg.setStatus(responseStatus);
//            }
//            if(msg != null) {
//                responseMsg.setMsg(msg);
//            }
//            if(message != null) {
//                responseMsg.setMessageType(message.getDescriptorForType().getFullName());
//                responseMsg.setMessage(message.toByteString());
//            }
//            this.responseMsg = responseMsg.build();
//            protocol.setMessage(this.responseMsg);
//        }
//        this.protocol = protocol.build();
//    }

    public static ResponseWrapper createHandShakeSuccessResponse(GameContext ctx, Message message, CryptoContext cryptoContext) {
        return new ResponseWrapper(ctx, GameProtocol.Status.SUCCESS, BaseMsgProtos.ResponseStatus.SUCCESS, null, message, cryptoContext);
    }

    public static ResponseWrapper createProtocolErrorResponse(GameContext ctx, GameProtocol.Status status) {
        return new ResponseWrapper(ctx, status, null, null, null, null);
    }


    private ResponseWrapper(GameContext ctx, GameProtocol.Status status, BaseMsgProtos.ResponseStatus responseStatus,
                           String msg, Message message, CryptoContext cryptoContext) {
        Preconditions.checkNotNull(status);

        GameProtocol requestProtocol = ctx.getRequest().getProtocol();

        GameProtocol.Builder protocol = GameProtocol.newBuilder();
        protocol.setPhase(requestProtocol.getPhase());
        protocol.setStatus(status);
        protocol.setResponseId(requestProtocol.getId());

        if(GameProtocol.Status.SUCCESS.equals(status)) {
            //只有正常解析协议了才设置业务对象
            BaseMsgProtos.ResponseMsg.Builder responseMsg = BaseMsgProtos.ResponseMsg.newBuilder();
            if(responseStatus != null) {
                responseMsg.setStatus(responseStatus);
            }
            if(msg != null) {
                responseMsg.setMsg(msg);
            }
            if(message != null) {
                responseMsg.setMessageType(message.getDescriptorForType().getFullName());
                responseMsg.setMessage(message.toByteString());
            }
            this.responseMsg = responseMsg.build();
            protocol.setMessage(this.responseMsg, cryptoContext);
        }
        this.protocol = protocol.build();
    }


    public BaseMsgProtos.ResponseMsg getResponseMsg() {
        return responseMsg;
    }

    public GameProtocol getProtocol() {
        return protocol;
    }

    public Message getMessage() {
        return message;
    }


    @Override
    public String toString() {
        return "ResponseWrapper{" +
                "protocol=" + protocol +
                ", responseMsg=" + responseMsg +
                ", message=" + message +
                '}';
    }
}
