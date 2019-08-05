package com.ranze.literpc.client.channel;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class ChannelFactory {
    private static ChannelFactory INSTANCE;

    private Bootstrap bootstrap;

    private ChannelFactory() {

    }

    public static ChannelFactory getInstance() {
        if (INSTANCE == null) {
            synchronized (ChannelFactory.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ChannelFactory();
                }
            }
        }
        return INSTANCE;
    }

    public void init(Bootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }


    public Channel create(InetSocketAddress address) {
        if (bootstrap == null) {
            log.warn("Create channel failed, bootstrap has not been set, call init() first");
            return null;
        }
        ChannelFuture channelFuture = bootstrap.connect(address);
        channelFuture.syncUninterruptibly();
        channelFuture.addListener(future -> {
            if (future.isSuccess()) {
                log.info("Create channel to {} success", address);
            } else {
                log.info("Create channel to {} failed, exception = {}", address, future.cause().getMessage());
            }
        });
        return channelFuture.channel();
    }
}
