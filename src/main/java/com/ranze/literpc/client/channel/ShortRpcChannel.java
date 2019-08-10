package com.ranze.literpc.client.channel;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class ShortRpcChannel implements RpcChannel {
    @Override
    public Channel get(InetSocketAddress address) {
        return ChannelFactory.getInstance().create(address);
    }

    @Override
    public void recycle(Channel channel) {
        if (channel.isActive()) {
            ChannelFuture closeFuture = channel.close();
            closeFuture.addListener(future -> {
                if (future.isSuccess()) {
                    log.info("Short channel closed success");
                } else
                    log.info("Short channel closed failed, exception: {}", future.cause().getMessage());
            });
        } else {
            log.info("Channel {} is not active, don't need to recycle");
        }

    }
}
