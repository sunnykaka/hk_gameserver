package com.origingame.server.protocol;

import com.google.protobuf.Message;
import com.origingame.exception.GameProtocolException;
import com.origingame.server.registry.DescriptorRegistry;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * User: liubin
 * Date: 14-3-5
 */
public class ProtocolUtil {

    private static final DescriptorRegistry descriptorRegistry = DescriptorRegistry.getInstance();

    public static Message parseMessageFromDataAndType(String messageType, byte[] messageBytes) {
        if(messageBytes.length == 0 || "".equals(messageType)) {
            return null;
        }
        Method messageParseMethod = descriptorRegistry.getMessageParseMethod(messageType);
        if(messageParseMethod == null) {
            throw new GameProtocolException(GameProtocol.Status.UNKNOWN_MESSAGE_TYPE, null);
        }
        try {
            Message message = (Message)messageParseMethod.invoke(null, messageBytes);
            return message;
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new GameProtocolException(GameProtocol.Status.DATA_CORRUPT, null);
        }
    }

}
