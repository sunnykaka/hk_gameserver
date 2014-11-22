package com.origingame.client.protocol;

import com.google.common.base.Preconditions;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.origingame.message.BaseMsgProtos;
import com.origingame.exception.GameProtocolException;
import com.origingame.server.protocol.GameProtocol;
import com.origingame.server.protocol.ProtocolUtil;
import com.origingame.util.crypto.CryptoContext;

/**
 * User: liubin
 * Date: 14-3-4
 */
public class ClientResponseWrapper {

    private GameProtocol protocol;

    private BaseMsgProtos.ResponseMsg responseMsg;

    private Message message;

    public ClientResponseWrapper() {}

    private boolean success;

    public static ClientResponseWrapper createResponseFromServer(GameProtocol protocol, byte[] passwordKey, boolean handShake) {

        ClientResponseWrapper response = new ClientResponseWrapper();
        response.protocol = protocol;
        Preconditions.checkArgument(GameProtocol.Type.RESPONSE.equals(protocol.getType()));
        byte[] data = protocol.getData();
        Preconditions.checkNotNull(data);
        response.success = GameProtocol.Status.SUCCESS.equals(protocol.getStatus());

        if(response.success) {
            //协议解析正确才解析内容
            CryptoContext cryptoContext = null;

            try {
                if(handShake) {
                    cryptoContext = CryptoContext.createRSAClientCrypto(passwordKey);
                } else {
                    cryptoContext = CryptoContext.createAESCrypto(passwordKey);
                }
                data = cryptoContext.decrypt(data);
            } catch (Exception e) {
                throw new GameProtocolException(GameProtocol.Status.DECIPHER_FAILED, protocol);
            }

            try {
                response.responseMsg = BaseMsgProtos.ResponseMsg.parseFrom(data);
                if(response.responseMsg.getMessageType() != null && response.responseMsg.getMessage() != null) {
                    response.message = ProtocolUtil.parseMessageFromDataAndType(response.responseMsg.getMessageType(), response.responseMsg.getMessage().toByteArray());
                }
            } catch (InvalidProtocolBufferException e) {
                throw new GameProtocolException(GameProtocol.Status.DATA_CORRUPT, protocol);
            }
        }


        return response;
    }


    public BaseMsgProtos.ResponseMsg getResponseMsg() {
        return responseMsg;
    }

    public GameProtocol getProtocol() {
        return protocol;
    }

    public boolean isSuccess() {
        return GameProtocol.Status.SUCCESS.equals(protocol.getStatus());
    }

    public boolean isNeedHandShakeAgain() {
        return GameProtocol.Status.HANDSHAKE_FAILED.equals(protocol.getStatus())
                || GameProtocol.Status.INVALID_ID.equals(protocol.getStatus())
                || GameProtocol.Status.INVALID_PLAYER_ID_IN_SESSION.equals(protocol.getStatus())
                || GameProtocol.Status.INVALID_SESSION_ID.equals(protocol.getStatus())
                ;
    }

    public Message getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "ClientResponseWrapper{" +
                "protocol=" + protocol +
                ", responseMsg=" + responseMsg +
                ", message=" + message +
                '}';
    }


}
