package com.ranze.literpc.codec;

import com.ranze.literpc.cons.Consts;
import com.ranze.literpc.protocol.Protocol;
import com.ranze.literpc.protocol.ProtocolType;
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
                RpcRequest rpcRequest = p.decodeRequest(byteBuf);
                if (rpcRequest != null) {
                    log.info("Find protocol to decode, protocol is {}", protocolEntry.getKey().getProtocol());
                    list.add(rpcRequest);
                    channelHandlerContext.channel().attr(Consts.KEY_PROTOCOL).set(p);
                }
            }
        } else {
            RpcRequest rpcRequest = protocol.decodeRequest(byteBuf);
            if (rpcRequest != null) {
                list.add(rpcRequest);
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
