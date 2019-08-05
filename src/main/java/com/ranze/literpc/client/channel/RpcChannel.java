package com.ranze.literpc.client.channel;

import io.netty.channel.Channel;

import java.net.InetSocketAddress;

public interface RpcChannel {
    Channel get(InetSocketAddress address);

    void recycle(Channel channel);
}
