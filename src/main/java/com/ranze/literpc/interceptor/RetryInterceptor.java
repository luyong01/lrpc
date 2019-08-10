package com.ranze.literpc.interceptor;

import com.ranze.literpc.exception.ErrorEnum;
import com.ranze.literpc.exception.RpcException;
import com.ranze.literpc.protocol.RpcRequest;
import com.ranze.literpc.protocol.RpcResponse;

public class RetryInterceptor implements Interceptor {
    private int retryCount;

    public RetryInterceptor(int retryCount) {
        this.retryCount = retryCount;
    }

    @Override
    public RpcResponse intercept(Chain chain) throws InterruptedException {
        RpcRequest rpcRequest = chain.rpcRequest();
        RpcResponse rpcResponse = chain.proceed(rpcRequest);
        RpcException exception = rpcResponse.getException();
        while (exception != null && exception.getCode() == ErrorEnum.TIMEOUT.getCode() && --retryCount != 0) {
            RpcResponse response = chain.proceed(rpcRequest);
            exception = response.getException();
        }
        return rpcResponse;
    }
}
