package com.ranze.literpc.example.server;

import com.ranze.literpc.example.service.HelloService;
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
        String response = request.getName().length() < 10 ?
                request.getName() : request.getName().substring(0, 10) + "...";
        return HelloServiceProto.HelloResponse.newBuilder()
                .setEcho("Hello, " + response + ", bye")
                .build();
    }

    @Override
    public HelloServiceProto.HelloResponse exceptionHello(HelloServiceProto.HelloRequest request) {
        throw new RuntimeException("exception hello");
    }
}
