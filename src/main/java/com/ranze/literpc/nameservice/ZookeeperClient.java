package com.ranze.literpc.nameservice;

import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.ZkClient;

import java.util.List;

@Slf4j
public class ZookeeperClient {
    private static volatile ZookeeperClient INSTANCE;

    private static final String ROOT_PATH = "/ranze";
    private static final String SERVER_PATH = ROOT_PATH + "/lrpc";

    private ZkClient zooKeeper;

    private String addressPath;

    private List<String> serverList;

    private ZookeeperClient(String address) {
        zooKeeper = new ZkClient(address);
    }

    public static ZookeeperClient getInstance(String address) {
        if (INSTANCE == null) {
            synchronized (ZookeeperClient.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ZookeeperClient(address);
                }
            }
        }
        return INSTANCE;
    }


    public void register(String ip, String port) {
        try {
            if (!zooKeeper.exists(ROOT_PATH)) {
                zooKeeper.createPersistent(ROOT_PATH);
            }

            if (!zooKeeper.exists(SERVER_PATH)) {
                zooKeeper.createPersistent(SERVER_PATH);
            }

            String address = ip + ":" + port;
            addressPath = SERVER_PATH + "/" + address;
            if (!zooKeeper.exists(addressPath)) {
                zooKeeper.createEphemeral(addressPath);
            }
        } catch (Exception e) {
            log.warn("Zookeeper create node caused exception: {}", e.getMessage());
            throw new RuntimeException(e);
        }

    }

    public List<String> getServerList() {
        return serverList;
    }

    public void updateChildren() {
        boolean serverExists = zooKeeper.exists(SERVER_PATH);
        if (serverExists) {
            serverList = zooKeeper.getChildren(SERVER_PATH);
            zooKeeper.subscribeChildChanges(SERVER_PATH, (parentPath, currentChilds) -> serverList = currentChilds);
        } else {
            log.warn("Server path {} is not exists");
        }
    }

}
