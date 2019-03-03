package com.ranze.literpc.example;

import com.ranze.literpc.server.LiteRpcServer;

public class LiteRpcServerTest {
    public static void main(String[] args) {
        LiteRpcServer liteRpcServer = new LiteRpcServer(8020);
        liteRpcServer.start();
    }
}
