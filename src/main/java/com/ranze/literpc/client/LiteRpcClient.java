package com.ranze.literpc.client;

import com.ranze.literpc.codec.RpcRequestEncoder;
import com.ranze.literpc.codec.RpcResponseDecoder;
import com.ranze.literpc.protocol.Protocol;
import com.ranze.literpc.protocol.ProtocolType;
import com.ranze.literpc.util.ClassUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
public class LiteRpcClient {
    private Map<Protocol.Type, Protocol> protocolMap;

    private Bootstrap bootstrap;
    private RpcClientOption rpcClientOption;

    private void initProtocolMap() {
        Set<Class<?>> classesWithAnnotation = ClassUtil.getClassesWithAnnotation("com.ranze.literpc.protocol", ProtocolType.class);
        protocolMap = new HashMap<>();
        for (Class<?> clz : classesWithAnnotation) {
            try {
                Object obj = clz.newInstance();
                if (obj instanceof Protocol) {
                    Protocol protocol = (Protocol) obj;
                    Protocol.Type type = protocol.getClass().getAnnotation(ProtocolType.class).value();
                    protocolMap.put(type, protocol);
                } else {
                    log.warn("Bean annotated with 'ProtocolType' must be sub type of interface 'Protocol'");
                }
            } catch (InstantiationException | IllegalAccessException e) {
                log.warn("Create new instance failed, msg = {}", e.getMessage());
            }
        }
    }

    public LiteRpcClient(RpcClientOption option) {
        initProtocolMap();

        this.rpcClientOption = option;

        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap
                .group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new RpcRequestEncoder(LiteRpcClient.this));
                        ch.pipeline().addLast(new RpcResponseDecoder());
                        ch.pipeline().addLast(new RpcClientHandler());
                    }
                });
    }

    public void sendRequest(Protocol.Type protoType) {

    }

    public Protocol getProtocol(Protocol.Type protoType) {
        return protocolMap.get(protoType);
    }


}
