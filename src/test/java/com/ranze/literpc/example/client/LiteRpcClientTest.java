package com.ranze.literpc.example.client;


import com.ranze.literpc.client.LiteRpcClient;
import com.ranze.literpc.client.RpcClientProxy;
import com.ranze.literpc.example.server.HelloServiceProto;
import com.ranze.literpc.example.service.HelloService;
import com.ranze.literpc.interceptor.Interceptor;
import com.ranze.literpc.protocol.RpcRequest;
import com.ranze.literpc.protocol.RpcResponse;

public class LiteRpcClientTest {
    public static void main(String[] args) {
        LiteRpcClient liteRpcClient = new LiteRpcClient();
        liteRpcClient.addInterceptor(new Interceptor() {
            @Override
            public RpcResponse intercept(Chain chain) throws InterruptedException {
                RpcRequest rpcRequest = chain.rpcRequest();
                System.out.println("Before proceed, request: " + rpcRequest);
                RpcResponse response = chain.proceed(rpcRequest);
                System.out.println("After proceed, response: " + response);
                return response;
            }
        });

        HelloService helloService = (HelloService) RpcClientProxy.newProxy(liteRpcClient, HelloService.class);

        HelloServiceProto.HelloRequest request = HelloServiceProto.HelloRequest.newBuilder()
                .setName("LRPC")
                .build();
        try {
            HelloServiceProto.HelloResponse response = helloService.hello(request);
            System.out.println("Response from remote: " + response.getEcho());
        } catch (Exception e) {
            System.out.println("Response error, " + e.getMessage());
        }

        try {
            liteRpcClient.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
