package com.ranze.literpc.client;

import com.ranze.literpc.protocol.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
    private LiteRpcClient client;

    public RpcClientHandler(LiteRpcClient liteRpcClient) {
        this.client = liteRpcClient;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse msg) throws Exception {
        log.debug("Received response msg: " + msg);
        if (msg == null) {
            log.warn("Response msg is null");
            return;
        }

        RpcFuture rpcFuture = client.getRpcFuture(msg.getCallId());
        client.removeRpcFuture(msg.getCallId());
        rpcFuture.handleResponse(msg);
        ctx.channel().close();


    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        ctx.channel().close();
    }
}
