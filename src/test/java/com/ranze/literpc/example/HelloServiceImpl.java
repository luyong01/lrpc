package com.ranze.literpc.example;

import com.ranze.literpc.server.Service;

@Service
public class HelloServiceImpl implements HelloService {

    @Override
    public HelloServiceProto.HelloResponse hello(HelloServiceProto.HelloRequest request) {
        return HelloServiceProto.HelloResponse.newBuilder()
                .setEcho("Hello, " + request.getName() + ", bye")
                .build();
    }
}
