package com.origingame.client.context;

import com.origingame.exception.GameClientException;
import com.origingame.server.protocol.GameProtocol;
import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;

/**
 * 消息上下文对象,用于取得跟id有关的上下文
 * User: liubin
 * Date: 14-3-16
 */
public class RequestChannelContext {

    private static final RequestChannelContext INSTANCE = new RequestChannelContext();
    private RequestChannelContext() {}
    public static final RequestChannelContext getInstance() {
        return INSTANCE;
    }

    //FIXME-内存泄漏点
    private ConcurrentMap<Channel, ConcurrentMap<Integer, TransferQueue<GameProtocol>>> channelMap = new ConcurrentHashMap<>();

    public TransferQueue<GameProtocol> addRequest(Channel channel, int id) {
        ConcurrentMap<Integer, TransferQueue<GameProtocol>> waiterQueueMap = channelMap.get(channel);
        if(waiterQueueMap == null) {
            waiterQueueMap = new ConcurrentHashMap<>();
            ConcurrentMap<Integer, TransferQueue<GameProtocol>> newWaiterQueueMap = channelMap.putIfAbsent(channel, waiterQueueMap);
            waiterQueueMap = newWaiterQueueMap == null ? waiterQueueMap : newWaiterQueueMap;
        }
        TransferQueue<GameProtocol> requestWaiterQueue = new LinkedTransferQueue<>();
        if(waiterQueueMap.put(id, requestWaiterQueue) != null) {
            throw new GameClientException(String.format("在存放requestId对应队列的时候,发现requestId[%d]冲突", id));
        }
        return requestWaiterQueue;
    }

    public TransferQueue<GameProtocol> getRequest(Channel channel, int id) {
        ConcurrentMap<Integer, TransferQueue<GameProtocol>> waiterQueueMap = channelMap.get(channel);
        if(waiterQueueMap == null) return null;
        return waiterQueueMap.get(id);
    }
}
