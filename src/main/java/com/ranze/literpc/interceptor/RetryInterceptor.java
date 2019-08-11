package com.ranze.literpc.interceptor;

import com.ranze.literpc.exception.ErrorEnum;
import com.ranze.literpc.exception.RpcException;
import com.ranze.literpc.protocol.RpcRequest;
import com.ranze.literpc.protocol.RpcResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RetryInterceptor implements Interceptor {
    private int retryCount;

    public RetryInterceptor(int retryCount) {
        this.retryCount = retryCount;
    }

    @Override
    public RpcResponse intercept(Chain chain) {
        RpcRequest rpcRequest = chain.rpcRequest();
        RpcException exception = null;
        RpcResponse rpcResponse = null;
        try {
            rpcResponse = chain.proceed(rpcRequest);
        } catch (RpcException e) {
            exception = e;
        }
        while (isRetryException(exception) && --retryCount != 0) {
            log.info("Request cause exception: {}, current retry count is {}", exception, retryCount);
            exception = null;
            try {
                rpcResponse = chain.proceed(rpcRequest);
            } catch (RpcException e) {
                exception = e;
            }
        }
        if (exception != null) {
            throw exception;
        }
        return rpcResponse;
    }

    private boolean isRetryException(RpcException exception) {
        if (exception != null) {
            int code = exception.getCode();
            return code == ErrorEnum.TIMEOUT.getCode() || code == ErrorEnum.NETWORK_ERROR.getCode();
        }
        return false;
    }
}
