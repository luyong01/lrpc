package com.ranze.literpc.client.channel;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.Getter;
import lombok.Setter;

import java.net.InetSocketAddress;

@Getter
@Setter
public class ChannelWrapper {
    private boolean inUse;
    private Channel channel;

    public ChannelWrapper(Channel channel) {
        inUse = false;
        this.channel = channel;

    }

}
