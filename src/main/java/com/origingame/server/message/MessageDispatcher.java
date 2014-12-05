package com.origingame.server.message;

import com.origingame.client.protocol.ClientRequestWrapper;
import com.origingame.client.protocol.ClientResponseWrapper;
import com.origingame.server.context.GameContext;
import com.origingame.exception.GameProtocolException;
import com.origingame.server.context.GameContextHolder;
import com.origingame.server.protocol.GameProtocol;
import com.origingame.server.protocol.ServerResponseWrapper;
import com.origingame.server.session.GameSession;
import com.origingame.server.main.World;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;


/**
 * 消息处理器.外部使用者只和这个对象打交道
 * User: liubin
 * Date: 14-3-16
 */
public class MessageDispatcher {

    private static final Logger log = LoggerFactory.getLogger(MessageDispatcher.class);

    private static final MessageDispatcher INSTANCE = new MessageDispatcher();

    ThreadPoolExecutor threadPoolExecutor;

    private MessageDispatcher() {
        threadPoolExecutor = new ThreadPoolExecutor(64,
                                                    64,
                                                    0L,
                                                    TimeUnit.SECONDS,
                                                    new LinkedBlockingQueue<Runnable>(10000),
                                                    new ThreadPoolExecutor.AbortPolicy()
                );
    }

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
    public void receive(final Channel channel, final GameProtocol protocol) {

        if(log.isDebugEnabled()) {
            log.debug("接收到{}消息, protocol[{}]",
                    (GameProtocol.Type.REQUEST.equals(protocol.getType()) ? "请求" : "响应"), protocol);
        }

        threadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (GameProtocol.Type.REQUEST.equals(protocol.getType())) {

                        //接收到的是请求消息,执行对应业务逻辑,如果有需要,返回响应结果
                        receiveRequest(channel, protocol);

                    } else {

                        //接收到的是响应消息
                        receiveResponse(channel, protocol);
                    }
                } catch (Throwable e) {
                    log.error("", e);
                }
            }
        });
    }

    private void receiveResponse(Channel channel, GameProtocol protocol) {
        messageReceiver.handleResponse(channel, protocol);
    }

    private void receiveRequest(Channel channel, GameProtocol protocol) {
        ServerResponseWrapper response = null;
        GameContext ctx = GameContextHolder.init();
        try {
            ctx.init(channel, protocol);

            response = messageReceiver.handleRequest(ctx);

            GameSession session = ctx.getSession();
            session.save();

        } catch (GameProtocolException e) {
            log.error(e.toString());
            GameProtocol.Status status = e.getStatus();
            response = ServerResponseWrapper.createProtocolErrorResponse(protocol, status == null ? GameProtocol.Status.OTHER_ERROR : status);
        } catch (Exception e) {
            log.error("对收到的消息进行解析的时候发生错误", e);
            response = ServerResponseWrapper.createProtocolErrorResponse(protocol, GameProtocol.Status.OTHER_ERROR);
        } finally  {
            //ctx 的清理工作
            GameContextHolder.destroy();
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
    public ClientResponseWrapper request(ClientRequestWrapper request) {
        return messageSender.sendRequest(request);
    }


}
