package com.ranze.literpc.client.channel;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class ChannelManager {
    private static ChannelManager INSTANCE;


    private ChannelManager() {
    }

    public void init(Bootstrap bootstrap) {
        ChannelFactory.getInstance().init(bootstrap);
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


    public Channel connect(InetSocketAddress address, ChannelType channelType) {
        if (channelType == ChannelType.SHORT) {
            return ChannelFactory.getInstance().create(address);
        } else if (channelType == ChannelType.POOLED) {
            return ChannelPoolGroup.getInstance().get(address);
        }
        return null;

    }

    public void recycle(ChannelType channelType, Channel channel) {
        if (channelType == ChannelType.SHORT) {
            if (channel.isActive()) {
                ChannelFuture closeFuture = channel.close();
                closeFuture.addListener(future -> {
                    if (future.isSuccess()) {
                        log.info("Short channel closed success");
                    } else
                        log.info("Short channel closed failed, exception: {}", future.cause().getMessage());
                });
            }
        } else if (channelType == ChannelType.POOLED) {
            ChannelPoolGroup.getInstance().recycle(channel);
        }

    }
}
