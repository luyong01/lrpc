[![Build Status](https://travis-ci.org/ranze16/lrpc.svg?branch=master)](https://travis-ci.org/ranze16/lrpc)
# lrpc

A lite RPC framework 

## 功能特性

- 支持短连接、连接池以及长连接
- 数据可压缩
- 提供拦截器
- 连接超时重试
- 设定数据传输大小上限
- 支持多协议以及协议扩展
- 服务端自动识别协议类型
- 接口可限流
- 基于 zookeeper 实现服务注册与发现
- 支持负载均衡

## 快速开始

**服务端配置**

```
service.package=com.ranze.literpc.example
service.port=8020
zookeeper.ip=127.0.0.1
zookeeper.port=2181
```

**服务端代码**
```java
public class LiteRpcServerTest {
    public static void main(String[] args) {
        LiteRpcServer liteRpcServer = new LiteRpcServer();
        liteRpcServer.start();
    }
}
```

**客户端配置**

```
service.protocol=lite_rpc
service.package=com.ranze.literpc.example
service.server.ip=127.0.0.1
service.server.port=8020
service.channel.type=pooled
service.compress=snappy
zookeeper.ip=127.0.0.1
zookeeper.port=2181
loadbalance=random
```

**客户端代码**

```java
public class LiteRpcClientTest {
    public static void main(String[] args) throws InterruptedException {
        LiteRpcClient liteRpcClient = new LiteRpcClient();
        liteRpcClient.addInterceptor(new Interceptor() {
            @Override
            public RpcResponse intercept(Chain chain) {
                RpcRequest rpcRequest = chain.rpcRequest();
                System.out.println("Before proceed, request: " + rpcRequest);
                RpcResponse response = chain.proceed(rpcRequest);
                System.out.println("After proceed, response: " + response);
                return response;
            }
        });

        // 获取代理类
        HelloService helloService = (HelloService) RpcClientProxy.newProxy(liteRpcClient, HelloService.class);

        HelloServiceProto.HelloRequest request = HelloServiceProto.HelloRequest.newBuilder()
                .setName("LRPC")
                .build();
        try {
            // 调用服务
            HelloServiceProto.HelloResponse response = helloService.hello(request);
            System.out.println("Get Response success: " + response.getEcho());
        } catch (Exception e) {
            System.out.println("Get Response error: " + e.getMessage());
        }

        liteRpcClient.shutdown();

    }

}
```



