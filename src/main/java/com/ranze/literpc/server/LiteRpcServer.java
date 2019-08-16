package com.ranze.literpc.server;

import com.ranze.literpc.codec.RpcRequestDecoder;
import com.ranze.literpc.codec.RpcResponseEncoder;
import com.ranze.literpc.nameservice.ZookeeperClient;
import com.ranze.literpc.protocol.Protocol;
import com.ranze.literpc.util.ProtocolUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class LiteRpcServer {
    private int port;

    private RpcServerOption rpcServerOption;

    private Map<Protocol.Type, Protocol> protocolMap;

    private ServerBootstrap serverBootstrap;

    private EventLoopGroup bossGroup;

    private EventLoopGroup workerGroup;
    private EventExecutorGroup eventExecutorGroup;

    private ZookeeperClient zookeeperClient;

    public LiteRpcServer() {
        this(new RpcServerOption("config-server.properties"));
    }

    public LiteRpcServer(RpcServerOption option) {
        rpcServerOption = option;
        protocolMap = new HashMap<>();
        ProtocolUtil.initProtocolMap(protocolMap);

        ServiceManager.getInstance().initServiceMap(option.getServiceImplPackage());
        ServiceManager.getInstance().initServiceIdKeyMap(option.getServicePackage());


        this.port = option.getPort();

        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();

        serverBootstrap = new ServerBootstrap();
        serverBootstrap
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new RpcRequestDecoder(LiteRpcServer.this));
                        socketChannel.pipeline().addLast(new RpcResponseEncoder());
                        socketChannel.pipeline().addLast(new RpcRequestHandler(LiteRpcServer.this));
                    }
                });

    }

    public void start() {
        try {
            serverBootstrap.bind(port).sync();

            String zookeeperAddress = rpcServerOption.getZookeeperIp() + ":" + rpcServerOption.getZookeeperPort();
            zookeeperClient = ZookeeperClient.getInstance(zookeeperAddress);
            zookeeperClient.register(getLocalIp(), rpcServerOption.getPort() + "");
        } catch (InterruptedException e) {
            log.error("Server failed to start, {}", e.getMessage());
            throw new RuntimeException(e);
        }
        log.info("Server started success, port = {}", port);
    }

    private String getLocalIp() {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            return addr.getHostAddress();
        } catch (UnknownHostException e) {
            log.warn("Get local ip caused exception: {}", e.getMessage());
            throw new RuntimeException(e);
        }

    }

    public Protocol getProtocol(Protocol.Type type) {
        return protocolMap.get(type);
    }

    public Map<Protocol.Type, Protocol> getProtocols() {
        return protocolMap;
    }
}
