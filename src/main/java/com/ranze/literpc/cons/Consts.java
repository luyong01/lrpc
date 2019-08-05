package com.ranze.literpc.cons;

import com.ranze.literpc.client.channel.ChannelPool;
import com.ranze.literpc.protocol.Protocol;
import io.netty.util.AttributeKey;

public class Consts {
    public static final AttributeKey<Protocol> KEY_PROTOCOL = AttributeKey.valueOf("protocol");
    public static final AttributeKey<ChannelPool> KEY_CHANNELPOOL = AttributeKey.valueOf("channelpool");
}
