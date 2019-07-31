package com.ranze.literpc.client;

import com.ranze.literpc.protocol.Protocol;
import com.ranze.literpc.protocol.ProtocolType;
import com.ranze.literpc.util.PropsUtil;
import com.ranze.literpc.util.ProtocolUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Getter
@Setter
public class RpcClientOption {
    private Protocol.Type protocolType;

    private Map<Protocol.Type, Protocol> protocolMap;

    public RpcClientOption(String configPath) {
        protocolMap = new HashMap<>();
        ProtocolUtil.initProtocolMap(protocolMap);

        Properties conf = PropsUtil.loadProps(configPath);
        String protocol = PropsUtil.getString(conf, "service.protocol");
        for (Protocol.Type p : protocolMap.keySet()) {
            if (p.getProtocol().equals(protocol)) {
                protocolType = p;
            }
        }

        if (protocolType == null) {
            throw new RuntimeException("Cannot find protocol for " + protocol);
        }
    }
}
