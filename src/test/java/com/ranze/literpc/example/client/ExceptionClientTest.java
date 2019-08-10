package com.ranze.literpc.example.client;


import com.ranze.literpc.client.LiteRpcClient;
import com.ranze.literpc.client.RpcClientProxy;
import com.ranze.literpc.example.server.HelloServiceProto;
import com.ranze.literpc.example.service.HelloService;

public class ExceptionClientTest {
    public static void main(String[] args) {
        LiteRpcClient liteRpcClient = new LiteRpcClient();

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
            HelloServiceProto.HelloResponse response = helloService.exceptionHello(request);
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
