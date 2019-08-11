package com.ranze.literpc.client;

import com.google.protobuf.Internal;
import com.ranze.literpc.client.channel.ChannelType;
import com.ranze.literpc.compress.Compress;
import com.ranze.literpc.interceptor.Interceptor;
import com.ranze.literpc.protocol.Protocol;
import com.ranze.literpc.util.PropsUtil;
import com.ranze.literpc.util.ProtocolUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
@Getter
@Setter
public class RpcClientOption {
    private ChannelType channelType;
    private Protocol.Type protocolType;
    private Compress.Type compressType;
    private String servicePackage;
    private String serverIp;
    private int serverPort;
    private int retryCount;
    private long timeOut; // 毫秒
    private List<Interceptor> interceptors;

    private Map<Protocol.Type, Protocol> protocolMap;

    public RpcClientOption(String configPath) {
        interceptors = new ArrayList<>();

        protocolMap = new HashMap<>();
        ProtocolUtil.initProtocolMap(protocolMap);

        Properties conf = PropsUtil.loadProps(configPath);

        servicePackage = PropsUtil.getString(conf, "service.package");
        serverIp = PropsUtil.getString(conf, "service.server.ip");
        serverPort = PropsUtil.getInt(conf, "service.server.port");

        retryCount = PropsUtil.getInt(conf, "service.retry_count", 3);
        timeOut = PropsUtil.getLong(conf, "service.time_out", 10000);

        String protocol = PropsUtil.getString(conf, "service.protocol", "lite_rpc");
        for (Protocol.Type p : protocolMap.keySet()) {
            if (p.getProtocol().equals(protocol)) {
                protocolType = p;
            }
        }
        if (protocolType == null) {
            throw new RuntimeException("Cannot find protocol for " + protocol);
        }

        String ct = PropsUtil.getString(conf, "service.channel.type", "pooled");
        switch (ct) {
            case "short":
                channelType = ChannelType.SHORT;
                break;
            case "pooled":
                channelType = ChannelType.POOLED;
                break;
            case "":
                channelType = ChannelType.POOLED;
                log.info("Channel type is unset, use pooled for default");
                break;
            default:
                throw new RuntimeException("Unrecognized channel type of " + ct);
        }

        String compressType = PropsUtil.getString(conf, "service.compress", "snappy");
        switch (compressType) {
            case "none":
                this.compressType = Compress.Type.NONE;
                break;
            case "gzip":
                this.compressType = Compress.Type.GZIP;
                break;
            case "snappy":
                this.compressType = Compress.Type.SNAPPY;
                break;
            default:
                throw new RuntimeException("Unsupported compress type: " + compressType);
        }
    }

    public String getServicePackage() {
        return servicePackage;
    }
}
