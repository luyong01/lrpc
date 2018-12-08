package com.ranze.literpc.protocol;

import io.netty.buffer.ByteBuf;

public interface Protocol {
    void encodeRequest(ByteBuf byteBuf, RpcRequest rpcRequest) throws Exception;

    RpcRequest decodeRequest(ByteBuf byteBuf) throws Exception;

    void encodeResponse(ByteBuf byteBuf, RpcResponse rpcResponse) throws Exception;

    RpcResponse decodeResponse(ByteBuf byteBuf) throws Exception;
}
