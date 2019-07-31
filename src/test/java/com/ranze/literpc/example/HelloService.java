package com.ranze.literpc.example;

public interface HelloService {
    HelloServiceProto.HelloResponse hello(HelloServiceProto.HelloRequest request);

    HelloServiceProto.HelloResponse exceptionHello(HelloServiceProto.HelloRequest request);
}
