package com.ranze.literpc.protocol;

import io.netty.buffer.ByteBuf;

public class LiteProtocol implements Protocol {


    @Override
    public void encodeRequest(ByteBuf byteBuf, RpcRequest rpcRequest) throws Exception {

    }

    @Override
    public RpcRequest decodeRequest(ByteBuf byteBuf) throws Exception {
        return null;
    }

    @Override
    public void encodeResponse(ByteBuf byteBuf, RpcResponse rpcResponse) throws Exception {

    }

    @Override
    public RpcResponse decodeResponse(ByteBuf byteBuf) throws Exception {
        return null;
    }
}
