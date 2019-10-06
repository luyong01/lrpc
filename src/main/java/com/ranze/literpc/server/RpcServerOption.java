package com.ranze.literpc.server;

import com.ranze.literpc.util.PropsUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Properties;

@Getter
@Setter
@ToString
public class RpcServerOption {
    private String servicePackage;
    private String serviceImplPackage;
    private int port;
    private long maxFrameLengthInBytes; // 最大数据包

    private String zookeeperIp;
    private String zookeeperPort;

    public RpcServerOption(String configPath) {
        Properties conf = PropsUtil.loadProps(configPath);
        port = PropsUtil.getInt(conf, "service.port");
        servicePackage = PropsUtil.getString(conf, "service.package");
        serviceImplPackage = PropsUtil.getString(conf, "service.impl.package");
        maxFrameLengthInBytes = PropsUtil.getLong(conf, "service.max_frame_length");

        zookeeperIp = PropsUtil.getString(conf, "zookeeper.ip");
        zookeeperPort = PropsUtil.getString(conf, "zookeeper.port");

    }
}
