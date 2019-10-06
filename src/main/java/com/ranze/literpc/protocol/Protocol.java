package com.ranze.literpc.protocol;

import com.ranze.literpc.client.LiteRpcClient;
import com.ranze.literpc.server.LiteRpcServer;
import io.netty.buffer.ByteBuf;


public interface Protocol {
    enum Type {
        LITE_RPC("lite_rpc"),
        INDEX_RPC("index_rpc");

        final String protocol;

        Type(String protocol) {
            this.protocol = protocol;
        }

        public String getProtocol() {
            return protocol;
        }
    }

    class DecodeResult {
        boolean correctProtocol;
        RpcRequest rpcRequest;
    }

    ByteBuf encodeRequest(RpcRequest rpcRequest) throws Exception;

    RpcRequest decodeRequest(ByteBuf byteBuf, LiteRpcServer rpcServer) throws Exception;

    ByteBuf encodeResponse(RpcResponse rpcResponse) throws Exception;

    RpcResponse decodeResponse(ByteBuf byteBuf, LiteRpcClient rpcClient) throws Exception;
}
