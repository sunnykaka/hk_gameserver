package com.origingame.server.protocol;

import com.google.common.base.Preconditions;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.origingame.message.BaseMsgProtos;
import com.origingame.message.HandShakeProtos;
import com.origingame.exception.CryptoException;
import com.origingame.exception.GameProtocolException;
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

    private Message message;

    private Channel channel;


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
        return requestMsg.getMessageType();
    }

    public int getPlayerId() {
        return requestMsg.getPlayerId();
    }

    public String getDeviceId() {
        return requestMsg.getDeviceId();
    }



    @Override
    public String toString() {
        return "ServerRequestWrapper{" +
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
