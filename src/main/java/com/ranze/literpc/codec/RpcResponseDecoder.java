package com.ranze.literpc.codec;

import com.ranze.literpc.client.LiteRpcClient;
import com.ranze.literpc.cons.Consts;
import com.ranze.literpc.protocol.Protocol;
import com.ranze.literpc.protocol.RpcResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class RpcResponseDecoder extends ByteToMessageDecoder {
    private LiteRpcClient liteRpcClient;

    public RpcResponseDecoder(LiteRpcClient liteRpcClient) {
        this.liteRpcClient = liteRpcClient;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        Protocol protocol = ctx.channel().attr(Consts.KEY_PROTOCOL).get();
        RpcResponse rpcResponse = protocol.decodeResponse(in, liteRpcClient);
        out.add(rpcResponse);
    }
}
