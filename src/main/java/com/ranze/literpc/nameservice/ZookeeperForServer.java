package com.ranze.literpc.nameservice;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;

import java.util.concurrent.CountDownLatch;

@Slf4j
public class ZookeeperForServer {
    private static ZookeeperForServer INSTANCE;

    private static final String ROOT_PATH = "/ranze";
    private static final String ROOT_NODE = "ranze";
    private static final String SERVER_PATH = "/lrpc";
    private static final String SERVER_NODE = "lrpc";

    private ZooKeeper zooKeeper;

    private CountDownLatch countDownLatch = new CountDownLatch(1);

    private ZookeeperForServer(String address) {
        zooKeeper = create(address);
        if (zooKeeper == null) {
            throw new RuntimeException("Create zookeeper failed");
        }
    }

    public static ZookeeperForServer getInstance(String address) {
        if (INSTANCE == null) {
            synchronized (ZookeeperForServer.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ZookeeperForServer(address);
                }
            }
        }
        return INSTANCE;
    }


    public ZooKeeper create(String address) {
        ZooKeeper zooKeeper = null;

        int timeout = 5000;
        try {
            zooKeeper = new ZooKeeper(address, timeout, new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    log.info("Zookeeper connect success");
                    countDownLatch.countDown();
                }
            });
            countDownLatch.await();
        } catch (Exception e) {
            log.warn("Zookeeper create caused exception: {}", e.getMessage());
        }
        return zooKeeper;
    }

    public void register(String ip, String port) {
        try {
            if (zooKeeper.exists(ROOT_PATH, false) == null) {
                zooKeeper.create(ROOT_PATH, ROOT_NODE.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }

            String serverPath = ROOT_PATH + SERVER_PATH;
            if (zooKeeper.exists(serverPath, false) == null) {
                zooKeeper.create(serverPath, SERVER_NODE.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }

            String address = ip + ":" + port;
            String addressPath = serverPath + "/" + address;
            if (zooKeeper.exists(addressPath, false) == null) {
                zooKeeper.create(addressPath, address.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            }
        } catch (Exception e) {
            log.warn("Zookeeper create node caused exception: {}", e.getMessage());
            throw new RuntimeException(e);
        }

    }

}
