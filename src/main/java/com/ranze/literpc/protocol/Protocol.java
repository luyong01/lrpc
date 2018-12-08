package com.ranze.literpc.protocol;

import io.netty.buffer.ByteBuf;


public interface Protocol {
    enum Type {
        LITE_PROTOCOL("lite_protocol");

        final String protocol;

        Type(String protocol) {
            this.protocol = protocol;
        }

        public String getProtocol() {
            return protocol;
        }
    }

    void encodeRequest(ByteBuf byteBuf, RpcRequest rpcRequest) throws Exception;

    RpcRequest decodeRequest(ByteBuf byteBuf) throws Exception;

    void encodeResponse(ByteBuf byteBuf, RpcResponse rpcResponse) throws Exception;

    RpcResponse decodeResponse(ByteBuf byteBuf) throws Exception;
}
