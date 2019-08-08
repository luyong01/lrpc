package com.ranze.literpc.example;


import com.ranze.literpc.client.LiteRpcClient;
import com.ranze.literpc.client.RpcClientProxy;
import com.ranze.literpc.exception.RpcException;

import java.util.concurrent.CountDownLatch;

public class MultiRequestClientTest {
    public static void main(String[] args) throws InterruptedException {
        LiteRpcClient liteRpcClient = new LiteRpcClient();


        int num = 5;
        CountDownLatch countDownLatchBegin = new CountDownLatch(1);
        CountDownLatch countDownLatchEnd = new CountDownLatch(num);
        for (int i = 0; i < num; ++i) {
            new Thread(new Request(liteRpcClient, countDownLatchBegin, countDownLatchEnd, i)).start();
        }

        countDownLatchBegin.countDown();
        countDownLatchEnd.await();

        try {
            liteRpcClient.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    static class Request implements Runnable {
        private LiteRpcClient rpcClient;
        private CountDownLatch countDownLatchBegin;
        private CountDownLatch countDownLatchEnd;
        private int index;

        Request(LiteRpcClient rpcClient, CountDownLatch countDownLatchBegin, CountDownLatch countDownLatchEnd, int index) {
            this.rpcClient = rpcClient;
            this.countDownLatchBegin = countDownLatchBegin;
            this.countDownLatchEnd = countDownLatchEnd;
            this.index = index;
        }

        @Override
        public void run() {
            try {
                countDownLatchBegin.await();
                System.out.println("[" + index + "] start send request");
                HelloService helloService = (HelloService) RpcClientProxy.newProxy(rpcClient, HelloService.class);

                HelloServiceProto.HelloRequest request = HelloServiceProto.HelloRequest.newBuilder()
                        .setName("LRPC")
                        .build();
                HelloServiceProto.HelloResponse response = helloService.hello(request);
                System.out.println("[" + index + "]Response from remote: " + response.getEcho());
            } catch (Exception e) {
                System.out.println("[" + index + "]Response error, " + e.getMessage());
            } finally {
                countDownLatchEnd.countDown();
            }

        }
    }

}
