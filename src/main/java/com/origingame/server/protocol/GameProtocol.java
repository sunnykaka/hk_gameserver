package com.origingame.server.protocol;

import com.google.common.primitives.UnsignedBytes;
import com.origingame.message.BaseMsgProtos;
import com.origingame.exception.GameProtocolException;
import com.origingame.util.crypto.CryptoContext;
import io.netty.buffer.ByteBuf;

/**
 * User: liubin
 * Date: 14-2-19
 */
public class GameProtocol {

    public static final int HEAD_LENGTH = 4 + 4 + 4 + 1 + 1 + 1;

    public static final GameProtocol REQUEST_FAILED = new GameProtocol();

    private GameProtocol() {}

    private GameProtocol(int dataLength, int sessionId, int id, Phase phase, Type type, Status status, byte[] data) {
        this.dataLength = dataLength;
        this.sessionId = sessionId;
        this.id = id;
        this.phase = phase;
        this.type = type;
        this.status = status;
        this.data = data;
    }

    /** 消息长度 4byte **/
    private int dataLength;

    /** 会话id 4byte **/
    private int sessionId;

    /** 请求/响应序号 4byte **/
    private int id;

    /** 当前协议阶段(0x0F握手请求，0x1F明文传输，0x2F加密传输) 1byte **/
    private Phase phase;

    /** 类型（0x0F request，0x1F response）1byte **/
    private Type type;

    /** 状态码(0x01-成功, 0x02-解密失败, 0x03-数据损坏, 0x04-未握手请求, 0xFF-其他错误)  1byte [only response] **/
    private Status status;

    /** 消息 **/
    private byte[] data;


    public static GameProtocol decode(ByteBuf in) {
        if(in.readableBytes() < HEAD_LENGTH) return null;
        int originReaderIndex = in.readerIndex();
        int dataLength = in.readInt();
        int sessionId = in.readInt();
        int id = in.readInt();
        Phase phase = Phase.valueOf(UnsignedBytes.toInt(in.readByte()));
        Type type = Type.valueOf(UnsignedBytes.toInt(in.readByte()));
        Status status = Status.valueOf(UnsignedBytes.toInt(in.readByte()));
        byte[] data = null;
        if(dataLength > 0) {
            if(in.readableBytes() < dataLength) {
                //数据没有读取完整
                in.readerIndex(originReaderIndex);
                return null;
            }
            data = new byte[dataLength];
            in.readBytes(data, 0, dataLength);
        }
        GameProtocol protocol = new GameProtocol(dataLength, sessionId, id, phase, type, status, data);
        return protocol;
    }

    public void encode(ByteBuf out) {
        out.writeInt(dataLength);
        out.writeInt(sessionId);
        out.writeInt(id);
        out.writeByte((byte)phase.value);
        out.writeByte((byte) type.value);
        out.writeByte(status == null ? 0 : (byte)status.value);
        if(dataLength > 0) {
            out.writeBytes(data, 0, dataLength);
        }
    }

    public enum Phase {

        HAND_SHAKE(0x0F),

        PLAIN_TEXT(0x1F),

        CIPHER_TEXT(0x2F);

        public int value;

        Phase(int value) {
            this.value = value;
        }

        public static Phase valueOf(int value) {
            if(value == 0) {
                return null;
            }
            for(Phase enumValue : values()) {
                if(value == enumValue.value) {
                    return enumValue;
                }
            }
            return null;
        }

    }

    public enum Status {

        /** 成功 **/
        SUCCESS(0),

        /** 解密失败 **/
        DECIPHER_FAILED(1),

        /** 数据损坏 **/
        DATA_CORRUPT(2),

        /** 不支持的协议阶段 **/
        INVALID_PHASE(3),

        /** 不支持的消息类型 **/
        UNKNOWN_MESSAGE_TYPE(4),

        /** 无效的会话,建议客户端重新握手**/
        INVALID_SESSION_ID(5),

        /** 无效的请求序号,建议客户端重新握手 **/
        INVALID_ID(6),

        /** 握手失败,建议客户端重新握手 **/
        HANDSHAKE_FAILED(7),

        /** 请求数据为空 **/
        REQUEST_MESSAGE_EMPTY(8),

        /** 重复的请求 **/
        REPEAT_ID(9),

        /** session校验失败, 玩家ID与登录时不匹配,建议客户端重新握手 **/
        INVALID_PLAYER_ID_IN_SESSION(10),

        /** 服务器内部错误 **/
        OTHER_ERROR(0xFF);

        public int value;

        Status(int value) {
            this.value = value;
        }

        public static Status valueOf(int value) {
            if(value == 0) {
                return null;
            }
            for(Status enumValue : values()) {
                if(value == enumValue.value) {
                    return enumValue;
                }
            }
            return null;
        }

    }

    public enum Type {

        REQUEST(0x0F),

        RESPONSE(0x1F);

        public int value;

        Type(int value) {
            this.value = value;
        }

        public static Type valueOf(int value) {
            if(value == 0) {
                return null;
            }
            for(Type enumValue : values()) {
                if(value == enumValue.value) {
                    return enumValue;
                }
            }
            return null;
        }

    }

    public int getDataLength() {
        return dataLength;
    }

    public int getId() {
        return id;
    }

    public Type getType() {
        return type;
    }

    public byte[] getData() {
        return data;
    }

    public Phase getPhase() {
        return phase;
    }

    public Status getStatus() {
        return status;
    }

    public int getSessionId() {
        return sessionId;
    }

    public static Builder newBuilder() {
        return Builder.create();
    }

    public static final class Builder {
        private GameProtocol protocol;

        private Builder() {
            protocol = new GameProtocol();
        }

        private static Builder create() {
            return new Builder();
        }

        public Builder setPhase(Phase phase) {
            protocol.phase = phase;
            return this;
        }

        public Builder setStatus(Status status) {
            protocol.status = status;
            return this;
        }

        public Builder setResponseId(int id) {
            protocol.type = Type.RESPONSE;
            protocol.id = id;
            return this;
        }

        public Builder setRequestId(int id) {
            protocol.type = Type.REQUEST;
            protocol.id = id;
            return this;
        }


        public Builder setSessionId(int sessionId) {
            protocol.sessionId = sessionId;
            return this;
        }

        public Builder setMessage(BaseMsgProtos.RequestMsg requestMsg, CryptoContext cryptoContext) {
            if(protocol.data != null || protocol.dataLength != 0) {
                throw new GameProtocolException("创建BlasterProtocol失败,为protocol添加message的时候发现内部data或messageType不为空");
            }
            protocol.type = Type.REQUEST;
            if(requestMsg != null) {
                byte[] data = requestMsg.toByteArray();
                if(cryptoContext != null) {
                    data = cryptoContext.encrypt(data);
                }
                protocol.data = data;
                protocol.dataLength = data.length;
            }
            return this;
        }

        public Builder setMessage(BaseMsgProtos.ResponseMsg responseMsg, CryptoContext cryptoContext) {
            if(protocol.data != null || protocol.dataLength != 0) {
                throw new GameProtocolException("创建BlasterProtocol失败,为protocol添加message的时候发现内部data或messageType不为空");
            }
            protocol.type = Type.RESPONSE;
            if(responseMsg != null) {
                byte[] data = responseMsg.toByteArray();
                if(cryptoContext != null) {
                    data = cryptoContext.encrypt(data);
                }
                protocol.data = data;
                protocol.dataLength = data.length;
            }
            return this;
        }

        public GameProtocol build() {
            if(Type.REQUEST.equals(protocol.type)) {
                //TODO 生成消息序号
//                protocol.id = IdGenerator.getRequestId();
            } else {
                if(protocol.id < 1) {
                    throw new GameProtocolException("创建BlasterProtocol失败,返回消息的id属性不能为空");
                }
            }
            return protocol;
        }
    }

}
