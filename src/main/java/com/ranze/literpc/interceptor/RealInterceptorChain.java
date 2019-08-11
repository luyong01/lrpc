package com.ranze.literpc.interceptor;

import com.ranze.literpc.protocol.RpcRequest;
import com.ranze.literpc.protocol.RpcResponse;

import java.util.List;

public class RealInterceptorChain implements Interceptor.Chain {
    private List<Interceptor> interceptors;
    private RpcRequest rpcRequest;
    private int index;

    public RealInterceptorChain(RpcRequest rpcRequest, List<Interceptor> interceptors, int index) {
        this.rpcRequest = rpcRequest;
        this.interceptors = interceptors;
        this.index = index;
    }

    @Override
    public RpcRequest rpcRequest() {
        return rpcRequest;
    }

    @Override
    public RpcResponse proceed(RpcRequest rpcRequest) {
        if (index >= interceptors.size()) {
            throw new RuntimeException("Index is bigger than interceptors size");
        }

        RealInterceptorChain next = new RealInterceptorChain(rpcRequest, interceptors, index + 1);
        Interceptor interceptor = interceptors.get(index);
        RpcResponse rpcResponse = interceptor.intercept(next);
        if (rpcResponse == null) {
            throw new RuntimeException("Interceptor " + interceptor + "returned null");
        }
        return rpcResponse;
    }
}
