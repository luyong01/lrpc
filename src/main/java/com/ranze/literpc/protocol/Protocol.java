package com.ranze.literpc.protocol;

import com.ranze.literpc.client.LiteRpcClient;
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

    ByteBuf encodeRequest(RpcRequest rpcRequest) throws Exception;

    RpcRequest decodeRequest(ByteBuf byteBuf) throws Exception;

    ByteBuf encodeResponse(RpcResponse rpcResponse) throws Exception;

    RpcResponse decodeResponse(ByteBuf byteBuf, LiteRpcClient rpcClient) throws Exception;
}
