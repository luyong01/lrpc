package com.ranze.literpc.client.channel;

import com.ranze.literpc.cons.Consts;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ChannelPoolGroup {
    private static volatile ChannelPoolGroup INSTANCE;

    private Map<InetSocketAddress, ChannelPool> channelPoolMap;

    private ChannelPoolGroup() {
        channelPoolMap = new HashMap<>();
    }

    public static ChannelPoolGroup getInstance() {
        if (INSTANCE == null) {
            synchronized (ChannelPoolGroup.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ChannelPoolGroup();
                }
            }
        }
        return INSTANCE;
    }


    public void update(List<InetSocketAddress> addresses) {
        for (InetSocketAddress address : addresses) {
            channelPoolMap.put(address, new ChannelPool(address));
        }
    }

    public Channel get(InetSocketAddress address) {
        ChannelPool channelPool = channelPoolMap.get(address);
        if (channelPool != null) {
            Channel channel = channelPool.get();
            channel.attr(Consts.KEY_CHANNELPOOL).setIfAbsent(channelPool);
            return channel;
        }
        return null;
    }

    public void recycle(Channel channel) {
        ChannelPool channelPool = channel.attr(Consts.KEY_CHANNELPOOL).get();
        if (channelPool != null) {
            channelPool.returnToPool(channel);
        } else {
            log.warn("Cannot get channel pool from channel {} to recycle channel", channel);
        }
    }
}
