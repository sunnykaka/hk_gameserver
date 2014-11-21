package com.origingame.server.message;

import com.origingame.server.context.GameContext;
import com.origingame.server.exception.GameProtocolException;
import com.origingame.server.protocol.GameProtocol;
import com.origingame.server.protocol.ServerRequestWrapper;
import com.origingame.server.protocol.ResponseWrapper;
import com.origingame.server.session.GameSession;
import com.origingame.server.main.World;
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

    /**
     * 接收响应
     * @param channel
     * @param protocol
     */
    public void receive(Channel channel, GameProtocol protocol) {

        if(GameProtocol.Type.REQUEST.equals(protocol.getType())) {

            //接收到的是请求消息,执行对应业务逻辑,如果有需要,返回响应结果
            receiveRequest(channel, protocol);

        } else {

            //接收到的是响应消息,查看是否注册了回调函数,有的话进行处理
            receiveResponse(channel, protocol);
        }

    }

    private void receiveResponse(Channel channel, GameProtocol protocol) {
        messageReceiver.handleResponse(new ResponseWrapper(protocol));
    }

    private void receiveRequest(Channel channel, GameProtocol protocol) {
        ResponseWrapper response = null;
        GameContext ctx = null;
        boolean protocolErrorHappened = false;
        try {
            ctx = new GameContext(channel, protocol);

            response = messageReceiver.handleRequest(ctx);

        } catch (GameProtocolException e) {
            log.error(e.toString());
            GameProtocol.Status status = e.getStatus();
            response = ResponseWrapper.createProtocolErrorResponse(ctx, status == null ? GameProtocol.Status.OTHER_ERROR : status);
            protocolErrorHappened = true;
        } catch (Exception e) {
            log.error("对收到的消息进行解析的时候发生错误", e);
            response = ResponseWrapper.createProtocolErrorResponse(ctx, GameProtocol.Status.OTHER_ERROR);
            protocolErrorHappened = true;
        } finally  {
            //TODO ctx, session 的一些提交和清理工作
            if(!protocolErrorHappened && ctx != null) {
                GameSession session = ctx.getSession();
                if(session != null && session.getBuilder() != null) {
                    session.getBuilder().setLastTime(World.now().getTime());
                    int id = ctx.getRequest().getProtocol().getId();
                    if(id > 0) {
                        session.getBuilder().setLastId(id);
                    }
                    session.save();
                }
            }
            if(ctx != null) {
                ctx.destroy();
            }
        }

        ctx.setResponse(response);

        if(response != null) {
            messageSender.sendResponse(ctx);
        }

    }

    /**
     * 发送请求(同步调用)
     * @param request
     */
    public ResponseWrapper request(ServerRequestWrapper request) {
        return messageSender.sendRequest(request);
    }


}
