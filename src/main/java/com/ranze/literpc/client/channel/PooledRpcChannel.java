package com.ranze.literpc.client.channel;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class PooledRpcChannel implements RpcChannel {
    @Override
    public Channel get(InetSocketAddress address) {
        return ChannelPoolGroup.getInstance().get(address);
    }

    @Override
    public void recycle(Channel channel) {
        ChannelPoolGroup.getInstance().recycle(channel);
    }
}
