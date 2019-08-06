package com.ranze.literpc.client;

import com.google.protobuf.Message;
import com.ranze.literpc.protocol.RpcRequest;
import com.ranze.literpc.protocol.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;

@Slf4j
public class RpcClientProxy implements InvocationHandler {
    private LiteRpcClient liteRpcClient;

    public RpcClientProxy(LiteRpcClient client) {
        this.liteRpcClient = client;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcRequest rpcRequest = new RpcRequest();

        long transactionId = liteRpcClient.getTransactionId();
        rpcRequest.setCallId(transactionId);
        rpcRequest.setMethod(method);
        // args 必须为 protobuf形式
        if (!Message.class.isAssignableFrom(args[0].getClass())) {
            log.error("Method arg must be protobuf");
            throw new RuntimeException("Method arg must be protobuf");
        }
        rpcRequest.setArgs((Message) args[0]);
        rpcRequest.setService(proxy.getClass().getInterfaces()[0]);
        rpcRequest.setCompressType(liteRpcClient.getOption().getCompressType());
        Type responseType = method.getGenericReturnType();
        RpcFuture rpcFuture = liteRpcClient.sendRequest(liteRpcClient.getOption().getProtocolType(),
                rpcRequest, responseType);
        RpcResponse response = rpcFuture.get();
        return response.getResult();
    }

    public static Object newProxy(LiteRpcClient client, Class<?> clz) {
        return Proxy.newProxyInstance(clz.getClassLoader(), new Class[]{clz},
                new RpcClientProxy(client));
    }

}
