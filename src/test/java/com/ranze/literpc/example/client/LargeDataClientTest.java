package com.ranze.literpc.example.client;

import com.ranze.literpc.client.LiteRpcClient;
import com.ranze.literpc.client.RpcClientProxy;
import com.ranze.literpc.example.server.HelloServiceProto;
import com.ranze.literpc.example.service.HelloService;

public class LargeDataClientTest {
    public static void main(String[] args) throws InterruptedException {

        LiteRpcClient liteRpcClient = new LiteRpcClient();

        // 获取代理类
        HelloService helloService = (HelloService) RpcClientProxy.newProxy(liteRpcClient, HelloService.class);

        String largeData = new String(new byte[1024 * 50]);
        HelloServiceProto.HelloRequest largeRequest = HelloServiceProto.HelloRequest.newBuilder()
                .setName(largeData)
                .build();

        HelloServiceProto.HelloRequest smallRequest = HelloServiceProto.HelloRequest.newBuilder()
                .setName("LRPC")
                .build();


        try {
            // 调用服务
            HelloServiceProto.HelloResponse response = helloService.hello(largeRequest);
            System.out.println("Get Response success: " + response.getEcho());
        } catch (Exception e) {
            System.out.println("Get Response error: " + e.getMessage());
        }

        try {
            // 调用服务
            HelloServiceProto.HelloResponse response = helloService.hello(smallRequest);
            System.out.println("Get Response success: " + response.getEcho());
        } catch (Exception e) {
            System.out.println("Get Response error: " + e.getMessage());
        }

        liteRpcClient.shutdown();

    }
}
