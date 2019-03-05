package com.ranze.literpc.client;

import com.ranze.literpc.exception.ErrorEnum;
import com.ranze.literpc.protocol.RpcResponse;
import com.ranze.literpc.exception.RpcException;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;
import java.util.concurrent.*;

@Slf4j
public class RpcFuture implements Future<RpcResponse> {
    private boolean isDone;
    private CountDownLatch countDownLatch;
    private RpcResponse rpcResponse;
    private Type responseType;

    public RpcFuture(Type responseType) {
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
    public RpcResponse get() throws InterruptedException, ExecutionException {
        countDownLatch.await();
        return rpcResponse;
    }

    @Override
    public RpcResponse get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (countDownLatch.await(timeout, unit)) {
            return rpcResponse;
        } else {
            log.warn("Get response time out");
            RpcResponse response = new RpcResponse();
            response.setException(new RpcException(ErrorEnum.TIMEOUT));
            return response;
        }
    }

    public Type getResponseType() {
        return responseType;
    }
}