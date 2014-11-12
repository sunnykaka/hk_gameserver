package com.origingame.server.message;

import com.rpg.rocket.blaster.context.IdContext;
import com.rpg.rocket.blaster.context.MessageContext;
import com.rpg.rocket.blaster.protocol.BlasterProtocol;
import com.rpg.rocket.blaster.protocol.RequestWrapper;
import com.rpg.rocket.blaster.protocol.ResponseWrapper;
import com.rpg.rocket.blaster.util.BlasterConstants;
import com.rpg.rocket.blaster.util.Clock;
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
public class BlasterSender {

    private static final Logger log = LoggerFactory.getLogger(BlasterSender.class);

    private static final BlasterSender INSTANCE = new BlasterSender();
    private BlasterSender() {}
    public static final BlasterSender getInstance() {
        return INSTANCE;
    }

    /**
     * 发送响应
     * @param channel
     * @param response
     */
    public void sendResponse(Channel channel, ResponseWrapper response) {
        BlasterProtocol protocol = response.getProtocol();
        if(protocol.getId() <= 0) {
            //id无效,无需返回结果
            log.warn("接收到id小于1的消息,id[{}],无法返回消息,消息内容:{}", protocol.getId(), response.toString());
            return;
        }
        if(log.isDebugEnabled()) {
            log.debug("准备发送响应, id[{}], response[{}]", new Object[]{protocol.getId(), response});
        }

        //XXX 为什么在这里用write就发送不了请求,即时设置了TCP_NODELAY也不行?
        //answer:因为write方法只是将消息加入了待发送队列,消息没有调用flush是不会发送的
        channel.writeAndFlush(protocol);
    }

    /**
     * 发送请求
     * @param channel
     * @param request
     * @param async
     * @param messageResponseHandler
     */
    public void sendRequest(Channel channel, RequestWrapper request, final boolean async, MessageResponseHandler messageResponseHandler) {

        final int id = request.getProtocol().getId();
        if(log.isDebugEnabled()) {
            log.debug("准备发送请求, id[{}], request[{}], async[{}], messageResponseHandler[{}]", new Object[]{id, request, async, messageResponseHandler});
        }

        //初始化id上下文信息
        final IdContext idContext = MessageContext.getInstance().initContext(id, async, request, messageResponseHandler);

        //XXX 如果writeAndFlush的参数是(request.getRequestMsg()),为什么operationComplete方法会直接被调用并且future.isSuccess是false?
        //answer:根据AbstractNioByteChannel.doWrite()方法,如果消息类型不是ByteBuffer或FileRegion,都会抛异常,然后框架内部捕获异常设置write结果失败
        //XXX 为什么在这里用write就发送不了请求,即时设置了TCP_NODELAY也不行?
        //answer:因为write方法只是将消息加入了待发送队列,消息没有调用flush是不会发送的
        channel.writeAndFlush(request.getProtocol()).addListener(new ChannelWriteFinishListener(id));

        if(!async) {
            try {
                if(messageResponseHandler == null) return;
                //如果是同步消息,需要等待响应返回
                ResponseWrapper response = null;
                TransferQueue<ResponseWrapper> requestWaiterQueue = idContext.getRequestWaiterQueue();
                long timeoutInMillseconds = request.getProtocol().getTimeout() - Clock.nowInMillisecond();
                if(timeoutInMillseconds > 0) {
                    try {
                        log.debug("同步请求开始等待返回结果,id[{}],等待时间[{}ms]", id, timeoutInMillseconds);
                        response = requestWaiterQueue.poll(timeoutInMillseconds, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        log.error("", e);
                    }
                } else {
                    //已超时,直接取结果
                    response = requestWaiterQueue.poll();
                }

                try {
                    BlasterSenderUtil.executeResponseHandler(request, response, messageResponseHandler);
                } catch (Exception e) {
                    log.error("同步发送消息并且进行结果处理的时候发生错误", e);
                }
            } finally {
                //同步请求由调用方负责清除数据
                MessageContext.getInstance().removeContext(id);
            }


        } else {
            //发送异步消息
            //TODO 需要对异步请求的超时情况处理,将超时的回调函数从map中取出,并执行超时的回调方法
        }

    }


    private static class ChannelWriteFinishListener implements ChannelFutureListener{

        private int id;

        private ChannelWriteFinishListener(int id) {
            this.id = id;
        }

        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
            if(log.isDebugEnabled()) {
                log.debug("发送请求结束, id[{}], success[{}]", new Object[]{id, future.isSuccess()});
            }
            if(future.isSuccess()) {
                return;
            }
            log.warn("发送请求信息失败, requestId[{}]", id);
            ResponseWrapper response = createSendRequestFailedResponse(id);
            MessageReceiver.getInstance().handleResponse(response);
        }


        /**
         * 创建发送消息失败的响应
         * @param id
         * @return
         */
        private ResponseWrapper createSendRequestFailedResponse(int id) {
            return new ResponseWrapper(BlasterConstants.PROTOCOL_VERSION, BlasterProtocol.Phase.PLAINTEXT, id,
                    BlasterProtocol.Status.REQUEST_FAILED, null, null, null);
        }
    }


}
