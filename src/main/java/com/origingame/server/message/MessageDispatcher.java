package com.origingame.server.message;

import com.origingame.server.context.GameContext;
import com.origingame.server.exception.GameProtocolException;
import com.origingame.server.protocol.GameProtocol;
import com.origingame.server.protocol.ResponseWrapper;
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

    MessageReceiver messageReceiver = MessageReceiver.getInstance();
    MessageSender messageSender = MessageSender.getInstance();
//    BlasterSender blasterSender = BlasterSender.getInstance();

    /**
     * 接收响应
     * @param channel
     * @param protocol
     */
    public void receive(Channel channel, GameProtocol protocol) {

        ResponseWrapper response = null;
        GameContext ctx = null;
        try {
            ctx = new GameContext(channel, protocol);

            response = messageReceiver.receive(ctx, protocol);

        } catch (GameProtocolException e) {
            log.error(e.toString());
            if(GameProtocol.Type.REQUEST.equals(protocol.getType())) {
                GameProtocol.Status status = e.getStatus();
                response = ResponseWrapper.createProtocolErrorResponse(ctx, status == null ? GameProtocol.Status.OTHER_ERROR : status);
            }
        } catch (Exception e) {
            log.error("对收到的消息进行解析的时候发生错误", e);
            if(GameProtocol.Type.REQUEST.equals(protocol.getType())) {

                response = ResponseWrapper.createProtocolErrorResponse(ctx, GameProtocol.Status.OTHER_ERROR);
            }
        } finally  {
            //TODO ctx, session 的一些提交和清理工作
        }

        ctx.setResponse(response);

        if(response != null) {
            messageSender.sendResponse(ctx);
        }



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
