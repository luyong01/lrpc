package com.ranze.literpc.client;

import com.ranze.literpc.codec.RpcRequestEncoder;
import com.ranze.literpc.codec.RpcResponseDecoder;
import com.ranze.literpc.cons.Consts;
import com.ranze.literpc.exception.ErrorEnum;
import com.ranze.literpc.protocol.Protocol;
import com.ranze.literpc.protocol.ProtocolType;
import com.ranze.literpc.protocol.RpcRequest;
import com.ranze.literpc.protocol.RpcResponse;
import com.ranze.literpc.util.ClassUtil;
import com.ranze.literpc.exception.RpcException;
import com.ranze.literpc.util.ProtocolUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;


import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class LiteRpcClient {
    private Map<Protocol.Type, Protocol> protocolMap;

    private AtomicLong transactionId;

    private Bootstrap bootstrap;
    private RpcClientOption rpcClientOption;

    private Map<String, Integer> remoteAddress;

    private ConcurrentHashMap<Long, RpcFuture> pendingRpcFutures;


    public LiteRpcClient(RpcClientOption option) {
        protocolMap = new HashMap<>();
        ProtocolUtil.initProtocolMap(protocolMap);

        pendingRpcFutures = new ConcurrentHashMap<>();

        transactionId = new AtomicLong(0);
        remoteAddress = new HashMap<>();
        remoteAddress.put("127.0.0.1", 8020);

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
                        ch.pipeline().addLast(new RpcResponseDecoder(LiteRpcClient.this));
                        ch.pipeline().addLast(new RpcClientHandler(LiteRpcClient.this));
                    }
                });
    }

    public RpcFuture sendRequest(Protocol.Type protoType, RpcRequest rpcRequest, Type responseType) {
        RpcFuture rpcFuture = new RpcFuture(responseType);
        pendingRpcFutures.put(rpcRequest.getCallId(), rpcFuture);

        String ip = "127.0.0.1";
        int port = 8020;
        ChannelFuture future = bootstrap.connect(new InetSocketAddress(ip, port));
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    log.debug("Connect to {}:{} success", ip, port);

                } else {
                    log.debug("Connect to {}:{} failed", ip, port);
                }
            }
        });
        future.syncUninterruptibly();
        if (future.isSuccess()) {
            future.channel().attr(Consts.KEY_PROTOCOL).set(protocolMap.get(protoType));
            ChannelFuture channelFuture = future.channel().writeAndFlush(rpcRequest);
            channelFuture.awaitUninterruptibly();
            if (!channelFuture.isSuccess()) {
                pendingRpcFutures.remove(rpcRequest.getCallId());
                log.warn("Write and flush data error");
                throw new RpcException(ErrorEnum.NETWORK_ERROR);
            } else {
                return rpcFuture;
            }
        } else {
            log.warn("Network error");
            throw new RpcException(ErrorEnum.NETWORK_ERROR);
        }

    }

    public long getTransactionId() {
        return transactionId.incrementAndGet();
    }

    public Protocol getProtocol(Protocol.Type protoType) {
        return protocolMap.get(protoType);
    }

    public RpcClientOption getOption() {
        return rpcClientOption;
    }

    public RpcFuture getRpcFuture(long callId) {
        return pendingRpcFutures.get(callId);
    }

    public boolean removeRpcFuture(long callId) {
        return null != pendingRpcFutures.remove(callId);
    }

}
