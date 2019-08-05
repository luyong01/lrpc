package com.ranze.literpc.client;

import com.ranze.literpc.client.channel.ChannelManager;
import com.ranze.literpc.protocol.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
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

        ChannelManager.getInstance().recycle(ctx.channel());
//        ctx.close().addListener(new GenericFutureListener<Future<? super Void>>() {
//            @Override
//            public void operationComplete(Future<? super Void> future) throws Exception {
//                if (future.isSuccess()) {
//                    log.info("channel close success");
//                } else {
//                    log.info("channel close failed");
//                }
//            }
//        });

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        ChannelManager.getInstance().recycle(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ChannelManager.getInstance().recycle(ctx.channel());
    }
}
