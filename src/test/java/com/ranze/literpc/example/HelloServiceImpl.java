package com.ranze.literpc.example;

import com.ranze.literpc.server.RateLimiter;
import com.ranze.literpc.server.Service;

@Service
public class HelloServiceImpl implements HelloService {

    @RateLimiter(LimitNum = 3)
    @Override
    public HelloServiceProto.HelloResponse hello(HelloServiceProto.HelloRequest request) {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return HelloServiceProto.HelloResponse.newBuilder()
                .setEcho("Hello, " + request.getName() + ", bye")
                .build();
    }

    @Override
    public HelloServiceProto.HelloResponse exceptionHello(HelloServiceProto.HelloRequest request) {
        throw new RuntimeException("exception hello");
    }
}
