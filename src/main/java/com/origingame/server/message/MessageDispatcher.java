package com.origingame.server.message;

import com.origingame.server.protocol.GameProtocol;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 消息处理器.外部使用者只和这个对象打交道
 * User: liubin
 * Date: 14-3-16
 */
public class MessageDispatcher {

    private static final Logger log = LoggerFactory.getLogger(MessageDispatcher.class);

    private static final MessageDispatcher INSTANCE = new MessageDispatcher();
    private MessageDispatcher() {}
    public static final MessageDispatcher getInstance() {
        return INSTANCE;
    }

//    MessageReceiver blasterReceiver = MessageReceiver.getInstance();
//    BlasterSender blasterSender = BlasterSender.getInstance();

    /**
     * 接收响应
     * @param channel
     * @param protocol
     */
    public void receive(Channel channel, GameProtocol protocol) {
//        ResponseWrapper response = null;
//        try {
//            response = blasterReceiver.receive(protocol);
//        } catch (BlasterProtocolException e) {
//            if(BlasterProtocol.Type.REQUEST.equals(protocol.getType())) {
//                BlasterProtocol.Status status = e.getStatus();
//                if(status == null) {
//                    status = BlasterProtocol.Status.OTHER;
//                }
//                response = new ResponseWrapper(BlasterConstants.PROTOCOL_VERSION, BlasterProtocol.Phase.PLAINTEXT, protocol.getId(), status, null, null, null);
//            }
//            log.warn(e.toString());
//        } catch (Exception e) {
//            log.error("对收到的消息进行解析的时候发生错误", e);
//        }
//
//        if(response != null) {
//            blasterSender.sendResponse(channel, response);
//        }
    }

//    /**
//     * 发送请求
//     * @param channel
//     * @param request
//     * @param async
//     * @param messageResponseHandler
//     */
//    public void request(Channel channel, RequestWrapper request, final boolean async, MessageResponseHandler messageResponseHandler) {
//        blasterSender.sendRequest(channel, request, async, messageResponseHandler);
//    }


}
