package com.ranze.literpc.interceptor;

import com.ranze.literpc.protocol.RpcRequest;
import com.ranze.literpc.protocol.RpcResponse;

public interface Interceptor {
    RpcResponse intercept(Chain chain);

    interface Chain {
        RpcRequest rpcRequest();

        RpcResponse proceed(RpcRequest rpcRequest);

    }

}
