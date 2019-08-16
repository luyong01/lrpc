package com.ranze.literpc.client.channel;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ChannelManager {
    private static ChannelManager INSTANCE;

    private RpcChannel rpcChannel;
    private long timeOut;
    private List<InetSocketAddress> addressList;

    private ChannelManager() {
        addressList = new ArrayList<>();
    }

    public void init(Bootstrap bootstrap, ChannelType channelType, long timeOut) {
        this.timeOut = timeOut;
        ChannelFactory.getInstance().init(bootstrap);
        if (channelType == ChannelType.SHORT) {
            rpcChannel = new ShortRpcChannel();
        } else if (channelType == ChannelType.POOLED) {
            rpcChannel = new PooledRpcChannel();
        } else if (channelType == ChannelType.SINGLETON) {
            rpcChannel = new SingletonRpcChannel();
        }
    }

    public static ChannelManager getInstance() {
        if (INSTANCE == null) {
            synchronized (ChannelManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ChannelManager();
                }
            }
        }
        return INSTANCE;
    }

    public void updateAddress(List<InetSocketAddress> addresses) {
        addressList = addresses;
        if (rpcChannel instanceof PooledRpcChannel) {
            PooledRpcChannel pooledRpcChannel = (PooledRpcChannel) rpcChannel;
            pooledRpcChannel.getChannelPoolGroup().update(addresses, timeOut);
        }

    }

    public Channel connect(InetSocketAddress address) {
        return rpcChannel.get(address);
    }

    public void recycle(Channel channel) {
        rpcChannel.recycle(channel);
    }
}
