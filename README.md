# lrpc

A lite RPC framework 

## 功能特性

- 提供短链接、连接池
- 数据可压缩
- 支持拦截器
- 连接超时重试
- 设定数据传输大小上限
- 支持多协议以及协议扩展
- 服务端自动识别协议类型
- 服务限流

## 快速开始

**服务端配置**

```
service.package=com.ranze.literpc.example
service.port=8020
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
```

**客户端代码**

```java
public class LiteRpcClientTest {
    public static void main(String[] args) {
        LiteRpcClient liteRpcClient = new LiteRpcClient();
        liteRpcClient.addInterceptor(new Interceptor() {
            @Override
            public RpcResponse intercept(Chain chain) throws InterruptedException {
                RpcRequest rpcRequest = chain.rpcRequest();
                System.out.println("Before proceed, request: " + rpcRequest);
                RpcResponse response = chain.proceed(rpcRequest);
                System.out.println("After proceed, response: " + response);
                return response;
            }
        });

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
            liteRpcClient.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
```



