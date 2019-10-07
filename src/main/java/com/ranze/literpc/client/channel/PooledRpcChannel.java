package com.ranze.literpc.client.channel;

import io.netty.channel.Channel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
@Getter
public class PooledRpcChannel implements RpcChannel {
    private ChannelPoolGroup channelPoolGroup;

    public PooledRpcChannel() {
        channelPoolGroup = ChannelPoolGroup.getInstance();
    }

    @Override
    public Channel get(InetSocketAddress address) {
        return channelPoolGroup.get(address);
    }

    @Override
    public void recycle(Channel channel) {
        channelPoolGroup.recycle(channel);
    }
}
