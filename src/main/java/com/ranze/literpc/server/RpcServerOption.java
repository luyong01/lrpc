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

    public RpcServerOption(String configPath) {
        Properties conf = PropsUtil.loadProps(configPath);
        port = PropsUtil.getInt(conf, "service.port");
        servicePackage = PropsUtil.getString(conf, "service.package");
        serviceImplPackage = PropsUtil.getString(conf, "service.impl.package");

    }
}
