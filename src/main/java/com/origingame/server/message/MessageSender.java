package com.origingame.server.message;

import com.origingame.client.context.RequestChannelContext;
import com.origingame.client.protocol.ClientRequestWrapper;
import com.origingame.client.protocol.ClientResponseWrapper;
import com.origingame.config.GlobalConfig;
import com.origingame.exception.RequestFailedException;
import com.origingame.exception.TimeoutException;
import com.origingame.server.context.GameContext;
import com.origingame.server.protocol.GameProtocol;
import com.origingame.server.protocol.ServerResponseWrapper;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TransferQueue;

/**
* 消息发送者
* User: liubin
* Date: 14-3-3
*/
public class MessageSender {

    private static final Logger log = LoggerFactory.getLogger(MessageSender.class);

    private static final MessageSender INSTANCE = new MessageSender();
    private MessageSender() {}
    public static final MessageSender getInstance() {
        return INSTANCE;
    }


    /**
     * 发送响应
     * @param ctx
     */
    public void sendResponse(GameContext ctx) {
        ServerResponseWrapper response = ctx.getResponse();
        GameProtocol protocol = response.getProtocol();
//        if(protocol.getId() <= 0) {
//            //id无效,无需返回结果
//            log.warn("接收到id小于1的消息,id[{}],无法返回消息,消息内容:{}", protocol.getId(), response.toString());
//            return;
//        }
        if(log.isDebugEnabled()) {
            log.debug("准备发送响应, id[{}], response[{}]", new Object[]{protocol.getId(), response});
        }

        ctx.getChannel().writeAndFlush(protocol);
    }

    public ClientResponseWrapper sendRequest(ClientRequestWrapper request) {

        Channel channel = request.getChannel();
        TransferQueue<GameProtocol> requestWaiterQueue = RequestChannelContext.getInstance().addRequest(channel, request.getRequestId());
        channel.writeAndFlush(request.getProtocol()).addListener(new ChannelWriteFinishListener(request.getProtocol()));
        GameProtocol responseProtocol = null;
        try {
            responseProtocol = requestWaiterQueue.poll(GlobalConfig.CLIENT_REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);

        } catch (InterruptedException e) {
            log.error("", e);
        }
        if(responseProtocol == null) {
            //超时
            throw new TimeoutException("请求超时,request:" + request);
        } else if(GameProtocol.Status.REQUEST_FAILED.equals(responseProtocol.getStatus())){
            //请求失败
            System.out.println("response:" + responseProtocol);
            throw new RequestFailedException("请求失败,request:" + request);
        } else {
            return ClientResponseWrapper.createResponseFromServer(responseProtocol, request.getPasswordKey(),
                    request.getPhase().equals(GameProtocol.Phase.HAND_SHAKE));
        }

    }


    private static class ChannelWriteFinishListener implements ChannelFutureListener {

        private GameProtocol protocol;

        private ChannelWriteFinishListener(GameProtocol protocol) {
            this.protocol = protocol;
        }

        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
            if(log.isDebugEnabled()) {
                log.debug("发送请求结束, protocol[{}], isSuccess[{}]", new Object[]{protocol, future.isSuccess()});
            }
            if(future.isSuccess()) {
                return;
            }
            MessageReceiver.getInstance().handleResponse(future.channel(), GameProtocol.newRequestFailed(protocol.getId()));
        }

    }


}
