package com.ranze.literpc.client;

import com.ranze.literpc.protocol.Protocol;
import com.ranze.literpc.protocol.ProtocolType;
import com.ranze.literpc.util.PropsUtil;
import com.ranze.literpc.util.ProtocolUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Slf4j
@Getter
@Setter
public class RpcClientOption {
    private ChannelType channelType;
    private Protocol.Type protocolType;
    private String serverIp;
    private int serverPort;

    private Map<Protocol.Type, Protocol> protocolMap;

    public RpcClientOption(String configPath) {
        protocolMap = new HashMap<>();
        ProtocolUtil.initProtocolMap(protocolMap);

        Properties conf = PropsUtil.loadProps(configPath);
        serverIp = PropsUtil.getString(conf, "service.server.ip");
        serverPort = PropsUtil.getInt(conf, "service.server.port");

        String protocol = PropsUtil.getString(conf, "service.protocol");
        for (Protocol.Type p : protocolMap.keySet()) {
            if (p.getProtocol().equals(protocol)) {
                protocolType = p;
            }
        }

        if (protocolType == null) {
            throw new RuntimeException("Cannot find protocol for " + protocol);
        }

        String ct = PropsUtil.getString(conf, "service.channel.type");
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
    }
}
