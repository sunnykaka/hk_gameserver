package com.origingame.server.message;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.origingame.message.BaseMsgProtos;
import com.origingame.message.HandShakeProtos;
import com.origingame.server.context.GameContext;
import com.origingame.server.exception.GameBusinessException;
import com.origingame.server.exception.GameProtocolException;
import com.origingame.server.protocol.GameProtocol;
import com.origingame.server.protocol.RequestWrapper;
import com.origingame.server.protocol.ResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.ws.handler.MessageContext;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TransferQueue;

/**
 * 消息接收者
 * User: liubin
 * Date: 14-3-3
 */
public class MessageReceiver {

    private static final Logger log = LoggerFactory.getLogger(MessageReceiver.class);

    private static final MessageReceiver INSTANCE = new MessageReceiver();
    private MessageReceiver() {}
    public static final MessageReceiver getInstance() {
        return INSTANCE;
    }

//    private MessageHandlerRegistry messageHandlerRegistry = MessageHandlerRegistry.getInstance();

    /**
     * 接收消息
     *
     * @param ctx
     * @param protocol
     * @return
     * @throws GameProtocolException
     */
    public ResponseWrapper receive(GameContext ctx, GameProtocol protocol) throws GameProtocolException {



        if(GameProtocol.Type.REQUEST.equals(protocol.getType())) {

            //接收到的是请求消息,执行对应业务逻辑,如果有需要,返回响应结果
            return handleRequest(new RequestWrapper(protocol));

        } else {

            //接收到的是响应消息,查看是否注册了回调函数,有的话进行处理
            handleResponse(new ResponseWrapper(protocol));
            return null;
        }

//
//        int id = protocol.getId();
//        int sessionId = protocol.getSessionId();
//
//        if(log.isDebugEnabled()) {
//            log.debug("接收到{}, sessionId[{}], id[{}], protocol[{}]", (GameProtocol.Type.REQUEST.equals(protocol.getType()) ? "请求" : "响应"), sessionId, id, protocol);
//        }
//
//        boolean decipher = false;
//
//
//
//
//
//
//        if(decipher) {
//            //TODO decipher
//        }
//
//        if(GameProtocol.Type.REQUEST.equals(protocol.getType())) {
//
//            //接收到的是请求消息,执行对应业务逻辑,如果有需要,返回响应结果
//            return handleRequest(new RequestWrapper(protocol));
//
//        } else {
//
//            //接收到的是响应消息,查看是否注册了回调函数,有的话进行处理
//            handleResponse(new ResponseWrapper(protocol));
//            return null;
//        }


    }

    /**
     * 处理请求消息
     * @param request
     * @return
     */
    protected ResponseWrapper handleRequest(RequestWrapper request) {
        GameProtocol protocol = request.getProtocol();
        int id = protocol.getId();

        switch (protocol.getPhase()) {
            case HAND_SHAKE: {
                try {
                    HandShakeProtos.HandShakeReq.parseFrom(protocol.getData());
                } catch (InvalidProtocolBufferException e) {
                    log.error("反序列化握手请求的时候发生错误");
                    throw new GameProtocolException(GameProtocol.Status.DATA_CORRUPT, protocol);
                }


                break;
            }
            case PLAIN_TEXT: {
                break;
            }
            case CIPHER_TEXT: {
                break;
            }
            default:
                throw new GameProtocolException(GameProtocol.Status.INVALID_PHASE, protocol);
        }



        BaseMsgProtos.RequestMsg requestMsg = request.getRequestMsg();
        Message message = request.getMessage();

//        MessageRequestHandler messageRequestHandler = messageHandlerRegistry.getMessageRequestHandler(requestMsg.getMessageType());
//        if(messageRequestHandler == null) {
//            log.warn("接收到类型为[{}]的消息,但是没有对应的请求处理器,消息内容:{}", requestMsg.getMessageType(), request.toString());
//            return null;
//        }

        Message result = null;
        boolean exceptionCaught = false;
        BaseMsgProtos.ResponseStatus responseStatus = BaseMsgProtos.ResponseStatus.UNKNOWN_ERROR;
        String responseMsg = null;

        try {
            if(log.isDebugEnabled()) {
                log.debug("执行请求,requestId[{}], request[{}]", id, request);
            }
            //执行业务处理
//            result = messageRequestHandler.handleRequest(request.getRequestInfo(), message);
            responseStatus = BaseMsgProtos.ResponseStatus.SUCCESS;
        } catch (GameBusinessException e) {
            exceptionCaught = true;
            if(e.getResponseStatus() != null) {
                responseStatus = e.getResponseStatus();
            }
            responseMsg = e.getMsg();
        } catch (Exception e) {
            exceptionCaught = true;
            log.error("", e);
        }
        if(!exceptionCaught && result == null) {
            //无响应消息
            return null;
        }

        return new ResponseWrapper(protocol.getPhase(), id, GameProtocol.Status.SUCCESS, responseStatus, responseMsg, result);
    }


    /**
     * 处理响应消息
     * @param response
     */
    protected void handleResponse(ResponseWrapper response) {
        int id = response.getProtocol().getId();
        GameProtocol protocol = response.getProtocol();

        log.error("现在的服务器不会处理从客户端发来的响应信息, id[{}], protocol[{}]", id, protocol);

        return;
    }

}
