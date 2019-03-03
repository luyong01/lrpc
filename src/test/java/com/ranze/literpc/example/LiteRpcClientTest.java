package com.ranze.literpc.example;


import com.ranze.literpc.client.LiteRpcClient;
import com.ranze.literpc.client.RpcClientOption;
import com.ranze.literpc.client.RpcClientProxy;

public class LiteRpcClientTest {
    public static void main(String[] args) {
        RpcClientOption option = new RpcClientOption();
        LiteRpcClient liteRpcClient = new LiteRpcClient(option);
        HelloService helloService = (HelloService) RpcClientProxy.newProxy(liteRpcClient, HelloService.class);

        HelloServiceProto.HelloRequest request = HelloServiceProto.HelloRequest.newBuilder()
                .setName("LRPC")
                .build();
        HelloServiceProto.HelloResponse response = helloService.hello(request);
        System.out.println("Response from remote: " + response.getEcho());

    }

}
