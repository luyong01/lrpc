package com.ranze.literpc.client.channel;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class ChannelManager {
    private static ChannelManager INSTANCE;

    private RpcChannel rpcChannel;

    private ChannelManager() {
    }

    public void init(Bootstrap bootstrap, ChannelType channelType) {
        ChannelFactory.getInstance().init(bootstrap);
        if (channelType == ChannelType.SHORT) {
            rpcChannel = new ShortRpcChannel();
        } else if (channelType == ChannelType.POOLED) {
            rpcChannel = new PooledRpcChannel();
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


    public Channel connect(InetSocketAddress address) {
        return rpcChannel.get(address);
    }

    public void recycle(Channel channel) {
        rpcChannel.recycle(channel);
    }
}
