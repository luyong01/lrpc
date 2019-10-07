package com.ranze.literpc.client;

import com.ranze.literpc.exception.ErrorEnum;
import com.ranze.literpc.exception.RpcException;
import com.ranze.literpc.protocol.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RpcFuture implements Future<RpcResponse> {
    private boolean isDone;
    private CountDownLatch countDownLatch;
    private RpcResponse rpcResponse;
    private Type responseType;
    private LiteRpcClient rpcClient;
    private long callId;

    public RpcFuture(LiteRpcClient rpcClient, long callId, Type responseType) {
        this.rpcClient = rpcClient;
        this.callId = callId;
        this.responseType = responseType;
        isDone = false;
        countDownLatch = new CountDownLatch(1);
    }

    public void handleResponse(RpcResponse rpcResponse) {
        this.rpcResponse = rpcResponse;
        isDone = true;
        countDownLatch.countDown();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return isDone;
    }

    @Override
    public RpcResponse get() {
        return get(rpcClient.getOption().getTimeOut(), TimeUnit.MILLISECONDS);
    }

    @Override
    public RpcResponse get(long timeout, TimeUnit unit) {
        try {
            if (countDownLatch.await(timeout, unit)) {
                if (rpcResponse.getException() != null) {
                    throw rpcResponse.getException();
                } else {
                    return rpcResponse;
                }
            } else {
                log.warn("Get response time out");
                throw new RpcException(ErrorEnum.TIMEOUT);
            }
        } catch (InterruptedException e) {
            log.warn("CountDownLatch await cause exception: {}", e.getMessage());
            throw new RuntimeException(e);
        } finally {
            rpcClient.removeRpcFuture(callId);
        }
    }

    public Type getResponseType() {
        return responseType;
    }
}
