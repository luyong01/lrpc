package com.ranze.literpc.client;

import com.ranze.literpc.protocol.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse msg) throws Exception {

    }
}
