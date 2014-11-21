package com.origingame.server.message;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import com.origingame.message.BaseMsgProtos;
import com.origingame.message.HandShakeProtos;
import com.origingame.server.action.ActionResolver;
import com.origingame.server.context.GameContext;
import com.origingame.server.exception.GameBusinessException;
import com.origingame.server.exception.GameProtocolException;
import com.origingame.server.protocol.GameProtocol;
import com.origingame.server.protocol.ServerRequestWrapper;
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
    private ActionResolver actionResolver = ActionResolver.getInstance();

//    private MessageHandlerRegistry messageHandlerRegistry = MessageHandlerRegistry.getInstance();

    /**
     * 处理请求消息
     * @param ctx
     * @return
     */
    protected ResponseWrapper handleRequest(GameContext ctx) {
        ServerRequestWrapper request = ctx.getRequest();
        GameSession session = ctx.getSession();
        GameProtocol protocol = request.getProtocol();

        switch (protocol.getPhase()) {
            case HAND_SHAKE: {
                request.parseHandShakeMessage();

                HandShakeProtos.HandShakeReq handShakeReq = (HandShakeProtos.HandShakeReq) request.getMessage();
                ByteString publicKey = handShakeReq.getPublicKey();
                if(session == null || !session.hasPublicKey(publicKey)) {
                    //根据sessionId和publicKey没有找到原来的密码记录,生成新的密码,并记录到session
                    if(session != null) {
                        session.invalid();
                    }
                    session = GameSession.create(ctx, publicKey);
                    ctx.setSession(session);
                    byte[] passwordKey = AES.initPasswordKey();
                    session.getBuilder().setPasswordKey(ByteString.copyFrom(passwordKey));
                }

                HandShakeProtos.HandShakeResp.Builder handShakeRespBuilder = HandShakeProtos.HandShakeResp.newBuilder();
                handShakeRespBuilder.setSessionId(session.getBuilder().getId());
                handShakeRespBuilder.setPasswordKey(session.getBuilder().getPasswordKey());
                return ResponseWrapper.createHandShakeSuccessResponse(ctx, handShakeRespBuilder.build(), CryptoContext.createRSAServerCrypto(publicKey.toByteArray()));

            }
            case PLAIN_TEXT: {
                throw new GameProtocolException(GameProtocol.Status.INVALID_PHASE, protocol);
            }
            case CIPHER_TEXT: {
                if(session == null) {
                    throw new GameProtocolException(GameProtocol.Status.INVALID_SESSION_ID, protocol);
                }
                //检验session合法性
                ctx.checkSession(protocol.getId(), request.getRequestMsg().getPlayerId(),
                        request.getDeviceId());

                byte[] passwordKey = session.getBuilder().getPasswordKey().toByteArray();
                request.parseCipherMessage(CryptoContext.createAESCrypto(passwordKey));

                //对message的业务处理
                return executeAction(ctx, request.getMessage());

            }
            default:
                throw new GameProtocolException(GameProtocol.Status.INVALID_PHASE, protocol);
        }

    }


    private ResponseWrapper executeAction(GameContext ctx, Message message) {

        ServerRequestWrapper request = ctx.getRequest();
        BaseMsgProtos.ResponseStatus responseStatus = BaseMsgProtos.ResponseStatus.UNKNOWN_ERROR;

        Message result = null;
        String responseMsg = null;

        try {
            if(log.isDebugEnabled()) {
                log.debug("接收到业务消息:sessionId[{}], id:[{}], playerId[{}], requestMsg[{}]",
                        request.getProtocol().getSessionId(), request.getProtocol().getId(),
                                request.getPlayerId(), request.getRequestMsg());
            }
            //执行业务处理
            result = actionResolver.executeAction0(ctx, message);
            responseStatus = BaseMsgProtos.ResponseStatus.SUCCESS;
        } catch (GameBusinessException e) {
            if(e.getResponseStatus() != null) {
                responseStatus = e.getResponseStatus();
            }
            responseMsg = e.getMsg();
        } catch (Exception e) {
            log.error("", e);
        }

//        if(!exceptionCaught && result == null) {
//            //无响应消息
//            return null;
//        }

        return ResponseWrapper.createRequestResponse(ctx, responseStatus, responseMsg, result,
                CryptoContext.createAESCrypto(ctx.getSession().getBuilder().getPasswordKey().toByteArray()));

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
