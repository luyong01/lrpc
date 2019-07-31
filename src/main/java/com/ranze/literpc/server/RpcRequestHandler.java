package com.ranze.literpc.server;

import com.google.protobuf.Message;
import com.ranze.literpc.exception.ErrorEnum;
import com.ranze.literpc.exception.RpcException;
import com.ranze.literpc.protocol.RpcRequest;
import com.ranze.literpc.protocol.RpcResponse;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

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
        log.info("Receive new request:{}", rpcRequest);
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setCallId(rpcRequest.getCallId());
        try {
            Object target = ServiceManager.getInstance().getService(rpcRequest.getService().getCanonicalName(),
                    rpcRequest.getMethod().getName()).getTarget();
            Message result = (Message) rpcRequest.getMethod().invoke(target, rpcRequest.getArgs());

            log.info("Process request {}, result={}", rpcRequest, Objects.toString(result));

            rpcResponse.setResult(result);
        } catch (InvocationTargetException e) {
            log.info("Precess request {} cause exception {}", rpcRequest, e.getMessage());
            rpcResponse.setException(new RpcException(ErrorEnum.SERVICE_EXCEPTION.getCode(), e.getTargetException().getMessage()));
        }


        ChannelFuture channelFuture = channelHandlerContext.channel().writeAndFlush(rpcResponse);
        channelFuture.addListener(new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
                if (future.isSuccess()) {
                    log.debug("Write response success");
                } else {
                    log.warn("Write response error {}", future.cause().getMessage());
                }
            }
        });

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        ctx.channel().close();
    }
}
