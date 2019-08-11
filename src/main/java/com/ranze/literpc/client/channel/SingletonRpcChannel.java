package com.ranze.literpc.client.channel;

import com.ranze.literpc.cons.Consts;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SingletonRpcChannel implements RpcChannel {
    // 每个 endpoint 一个连接
    private Map<InetSocketAddress, Channel> channelMap;

    public SingletonRpcChannel() {
        channelMap = new ConcurrentHashMap<>();

    }

    @Override

    public Channel get(InetSocketAddress address) {
        Channel channel = channelMap.get(address);
        if (channel == null) {
            synchronized (SingletonRpcChannel.this) {
                channel = channelMap.get(address);
                if (channel == null) {
                    channel = ChannelFactory.getInstance().create(address);
                    if (channel != null) {
                        channel.attr(Consts.KEY_ADDRESS).set(address);
                        channelMap.put(address, channel);
                    }
                }
            }
        }
        log.info("Singleton channel to get, channel for address{} is {}", address, Objects.toString(channel));
        return channel;
    }

    @Override
    public void recycle(Channel channel) {
        log.info("Singleton channel to recycle");
        if (channel != null && !channel.isActive()) {
            InetSocketAddress address = channel.attr(Consts.KEY_ADDRESS).get();
            channelMap.remove(address);
        }

    }
}
