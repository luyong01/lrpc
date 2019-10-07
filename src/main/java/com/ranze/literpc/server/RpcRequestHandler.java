package com.ranze.literpc.server;

import com.ranze.literpc.protocol.RpcRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcRequestHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private LiteRpcServer liteRpcServer;

    public RpcRequestHandler(LiteRpcServer liteRpcServer) {
        this.liteRpcServer = liteRpcServer;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest) throws Exception {
        if (rpcRequest == null) {
            log.warn("Request is null");
            return;
        }
        log.info("Thread: {}, Receive new request:{}", Thread.currentThread().getName(), rpcRequest);

        WorkerThreadPool.getInstance().submit(new WorkerThreadPool.Task(channelHandlerContext, rpcRequest));
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        ctx.channel().close();
    }
}
