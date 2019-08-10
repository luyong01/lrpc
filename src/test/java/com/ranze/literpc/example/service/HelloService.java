package com.ranze.literpc.example.service;

import com.ranze.literpc.example.server.HelloServiceProto;

public interface HelloService {
    HelloServiceProto.HelloResponse hello(HelloServiceProto.HelloRequest request);

    HelloServiceProto.HelloResponse exceptionHello(HelloServiceProto.HelloRequest request);
}
