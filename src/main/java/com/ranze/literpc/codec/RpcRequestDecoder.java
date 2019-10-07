package com.ranze.literpc.codec;

import com.ranze.literpc.cons.Consts;
import com.ranze.literpc.exception.ExceedFrameLenException;
import com.ranze.literpc.protocol.Protocol;
import com.ranze.literpc.protocol.RpcRequest;
import com.ranze.literpc.server.LiteRpcServer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
public class RpcRequestDecoder extends ByteToMessageDecoder {
    private LiteRpcServer liteRpcServer;

    public RpcRequestDecoder(LiteRpcServer liteRpcServer) {
        this.liteRpcServer = liteRpcServer;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        Protocol protocol = channelHandlerContext.channel().attr(Consts.KEY_PROTOCOL).get();
        if (protocol == null) {
            log.info("Can't find protocol from this channel, search from all protocols");
            for (Map.Entry<Protocol.Type, Protocol> protocolEntry : liteRpcServer.getProtocols().entrySet()) {
                Protocol p = protocolEntry.getValue();
                RpcRequest rpcRequest = null;

                try {
                    rpcRequest = p.decodeRequest(byteBuf, liteRpcServer);
                } catch (ExceedFrameLenException e) {
                    // 如果抛出这个异常，说明协议解析对了，但是长度超出
                    log.info("Find protocol to decode, but frame length is too long, protocol is {}", protocolEntry.getKey().getProtocol());
                    channelHandlerContext.channel().attr(Consts.KEY_PROTOCOL).set(p);
                    break;
                }

                if (rpcRequest != null) {
                    log.info("Find protocol to decode, protocol is {}", protocolEntry.getKey().getProtocol());
                    list.add(rpcRequest);
                    channelHandlerContext.channel().attr(Consts.KEY_PROTOCOL).set(p);
                    break;
                }
            }
        } else {
            try {
                RpcRequest rpcRequest = protocol.decodeRequest(byteBuf, liteRpcServer);
                if (rpcRequest != null) {
                    list.add(rpcRequest);
                }
            } catch (ExceedFrameLenException e) {
                log.info("Exceed frame len exception");
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        log.info("Channel exception caught: {}", cause.getMessage());
        ctx.close();
    }
}
