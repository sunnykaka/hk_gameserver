package com.origingame.server.protocol;

import com.google.common.primitives.UnsignedBytes;
import com.origingame.message.BaseMsgProtos;
import com.origingame.server.exception.GameProtocolException;
import io.netty.buffer.ByteBuf;

/**
 * User: liubin
 * Date: 14-2-19
 */
public class GameProtocol {

    public static final int HEAD_LENGTH = 4 + 4 + 1 + 1 + 1;


    private GameProtocol() {}

    private GameProtocol(int dataLength, int id, Phase phase, Type type, Status status, byte[] data) {
        this.dataLength = dataLength;
        this.id = id;
        this.phase = phase;
        this.type = type;
        this.status = status;
        this.data = data;
    }

    /** 消息长度 4byte **/
    private int dataLength;

    /** 请求/响应ID 4byte **/
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
        GameProtocol protocol = new GameProtocol(dataLength, id, phase, type, status, data);
        return protocol;
    }

    public void encode(ByteBuf out) {
        out.writeInt(dataLength);
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
        SUCCESS(0x01),

        /** 解密失败 **/
        DECIPHER_FAILED(0x02),

        /** 数据损坏 **/
        DATA_CORRUPT(0x03),

        /** 不支持的协议阶段 **/
        INVALID_PHASE(0x04),

        /** 不支持的消息类型 **/
        UNKNOWN_MESSAGE_TYPE(0x05),

        /** 其他错误 **/
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

        public Builder setMessage(BaseMsgProtos.RequestMsg requestMsg) {
            if(protocol.data != null || protocol.dataLength != 0) {
                throw new GameProtocolException("创建BlasterProtocol失败,为protocol添加message的时候发现内部data或messageType不为空");
            }
            protocol.type = Type.REQUEST;
            if(requestMsg != null) {
                byte[] data = requestMsg.toByteArray();
                protocol.data = data;
                protocol.dataLength = data.length;
            }
            return this;
        }

        public Builder setMessage(BaseMsgProtos.ResponseMsg responseMsg) {
            if(protocol.data != null || protocol.dataLength != 0) {
                throw new GameProtocolException("创建BlasterProtocol失败,为protocol添加message的时候发现内部data或messageType不为空");
            }
            protocol.type = Type.RESPONSE;
            if(responseMsg != null) {
                byte[] data = responseMsg.toByteArray();
                protocol.data = data;
                protocol.dataLength = data.length;
            }
            return this;
        }

        public GameProtocol build() {
            if(Type.REQUEST.equals(protocol.type)) {
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
