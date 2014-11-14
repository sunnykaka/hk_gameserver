package com.origingame.server.message;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import com.origingame.message.BaseMsgProtos;
import com.origingame.message.HandShakeProtos;
import com.origingame.server.context.GameContext;
import com.origingame.server.exception.GameProtocolException;
import com.origingame.server.protocol.GameProtocol;
import com.origingame.server.protocol.RequestWrapper;
import com.origingame.server.protocol.ResponseWrapper;
import com.origingame.server.session.GameSession;
import com.origingame.server.session.LocalGameSessionMgrImpl;
import com.origingame.util.crypto.AES;
import com.origingame.util.crypto.CryptoContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private LocalGameSessionMgrImpl gameSessionMgr = LocalGameSessionMgrImpl.getInstance();

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
            return handleRequest(ctx);

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
     * @param ctx
     * @return
     */
    protected ResponseWrapper handleRequest(GameContext ctx) {
        RequestWrapper request = ctx.getRequest();
        GameSession session = ctx.getSession();
        GameProtocol protocol = request.getProtocol();

        switch (protocol.getPhase()) {
            case HAND_SHAKE: {
                request.parseHandShakeMessage();

                HandShakeProtos.HandShakeReq handShakeReq = (HandShakeProtos.HandShakeReq) request.getMessage();
                byte[] publicKey = handShakeReq.getPublicKey().toByteArray();
                if(session == null || !session.hasPublicKey(publicKey)) {
                    //根据sessionId和publicKey没有找到原来的密码记录,生成新的密码,并记录到session
                    if(session != null) {
                        gameSessionMgr.invalid(session);
                    }
                    session = gameSessionMgr.init(ctx, publicKey);
                    byte[] passwordKey = AES.initPasswordKey();
                    session.setPasswordKey(passwordKey);
                }

                HandShakeProtos.HandShakeResp.Builder handShakeRespBuilder = HandShakeProtos.HandShakeResp.newBuilder();
                handShakeRespBuilder.setSessionId(session.getSessionId());
                handShakeRespBuilder.setPasswordKey(ByteString.copyFrom(session.getPasswordKey()));
                return ResponseWrapper.createHandShakeSuccessResponse(ctx, handShakeRespBuilder.build(), CryptoContext.createRSAServerCrypto(publicKey));

            }
            case PLAIN_TEXT: {
                break;
            }
            case CIPHER_TEXT: {
                if(session == null) {
                    throw new GameProtocolException(GameProtocol.Status.INVALID_SESSION_ID, protocol);
                }
                //TODO 检验session合法性

                byte[] passwordKey = session.getPasswordKey();
                request.parseCipherMessage(CryptoContext.createAESCrypto(passwordKey));
                BaseMsgProtos.RequestMsg requestMsg = request.getRequestMsg();
                Message message = request.getMessage();

                System.out.println(String.format("接收到业务消息:[%s], 类型:[%s], 长度[%d]", message.toString(), requestMsg.getMessageType(), message.getSerializedSize()));

                //TODO 对message的业务处理




//                CryptoContext.createRSAServerCrypto()


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
//
//        Message result = null;
//        boolean exceptionCaught = false;
//        BaseMsgProtos.ResponseStatus responseStatus = BaseMsgProtos.ResponseStatus.UNKNOWN_ERROR;
//        String responseMsg = null;
//
//        try {
//            if(log.isDebugEnabled()) {
//                log.debug("执行请求,requestId[{}], request[{}]", id, request);
//            }
//            //执行业务处理
////            result = messageRequestHandler.handleRequest(request.getRequestInfo(), message);
//            responseStatus = BaseMsgProtos.ResponseStatus.SUCCESS;
//        } catch (GameBusinessException e) {
//            exceptionCaught = true;
//            if(e.getResponseStatus() != null) {
//                responseStatus = e.getResponseStatus();
//            }
//            responseMsg = e.getMsg();
//        } catch (Exception e) {
//            exceptionCaught = true;
//            log.error("", e);
//        }
//        if(!exceptionCaught && result == null) {
//            //无响应消息
//            return null;
//        }
//
//        return new ResponseWrapper(protocol.getPhase(), id, GameProtocol.Status.SUCCESS, responseStatus, responseMsg, result);

        return null;
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
