package com.ranze.literpc.interceptor;

import com.ranze.literpc.client.LiteRpcClient;
import com.ranze.literpc.client.RpcFuture;
import com.ranze.literpc.protocol.RpcRequest;
import com.ranze.literpc.protocol.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;

@Slf4j
public class ConnectInterceptor implements Interceptor {
    private LiteRpcClient liteRpcClient;
    private Type responseType;

    public ConnectInterceptor(LiteRpcClient liteRpcClient, Type responseType) {
        this.liteRpcClient = liteRpcClient;
        this.responseType = responseType;
    }

    @Override
    public RpcResponse intercept(Chain chain) throws InterruptedException {
        RpcRequest rpcRequest = chain.rpcRequest();
        RpcFuture rpcFuture = liteRpcClient.sendRequest(liteRpcClient.getOption().getProtocolType(),
                rpcRequest, responseType);
        log.info("Request has been sent {}", rpcRequest);
        return rpcFuture.get();
    }
}
