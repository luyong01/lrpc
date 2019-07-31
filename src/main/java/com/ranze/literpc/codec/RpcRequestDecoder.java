package com.ranze.literpc.codec;

import com.ranze.literpc.cons.Consts;
import com.ranze.literpc.protocol.Protocol;
import com.ranze.literpc.protocol.RpcRequest;
import com.ranze.literpc.server.LiteRpcServer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class RpcRequestDecoder extends ByteToMessageDecoder {
    private LiteRpcServer liteRpcServer;

    public RpcRequestDecoder(LiteRpcServer liteRpcServer) {
        this.liteRpcServer = liteRpcServer;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        Protocol protocol = channelHandlerContext.channel().attr(Consts.KEY_PROTOCOL).get();
        if (protocol == null) {
            protocol = liteRpcServer.getProtocol(Protocol.Type.LITE_RPC);
            channelHandlerContext.channel().attr(Consts.KEY_PROTOCOL)
                    .set(liteRpcServer.getProtocol(Protocol.Type.LITE_RPC));
        }

        RpcRequest rpcRequest = protocol.decodeRequest(byteBuf);
        if (rpcRequest != null) {
            list.add(rpcRequest);
        }
    }
}
