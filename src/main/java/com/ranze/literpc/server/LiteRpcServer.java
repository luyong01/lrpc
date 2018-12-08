package com.ranze.literpc.server;

import com.ranze.literpc.codec.RpcRequestDecoder;
import com.ranze.literpc.codec.RpcResponseEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LiteRpcServer {
    private int port;

    private ServerBootstrap serverBootstrap;

    private EventLoopGroup bossGroup;

    private EventLoopGroup workerGroup;
    private EventExecutorGroup eventExecutorGroup;

    public LiteRpcServer(int port) {
        this.port = port;

        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        eventExecutorGroup = new DefaultEventExecutorGroup(10);

        serverBootstrap = new ServerBootstrap();
        serverBootstrap
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new ChannelInitializer<NioServerSocketChannel>() {
                    @Override
                    protected void initChannel(NioServerSocketChannel nioServerSocketChannel) throws Exception {
                        nioServerSocketChannel.pipeline().addLast(new RpcRequestDecoder());
                        nioServerSocketChannel.pipeline().addLast(new RpcResponseEncoder());
                        nioServerSocketChannel.pipeline().addLast(
                                eventExecutorGroup,
                                "Request Handler",
                                new RpcRequestHandler());
                    }
                });
    }

    public void start() {
        try {
            serverBootstrap.bind(port).sync();
        } catch (InterruptedException e) {
            log.error("Server failed to start, {}", e.getMessage());
        }
        log.info("Server started success, port = {}", port);
    }
}
