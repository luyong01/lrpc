package com.ranze.literpc.client.loadbanlance;

import java.util.List;

public class LoadBalanceManager {
    private static volatile LoadBalanceManager INSTANCE;
    private LoadBalancePolicy loadBalancePolicy;

    private LoadBalanceManager(LoadBalancePolicy.Type type) {
        if (type == LoadBalancePolicy.Type.ROUND_ROBIN) {
            loadBalancePolicy = new RoundRobinPolicy();
        } else if (type == LoadBalancePolicy.Type.RANDOM) {
            loadBalancePolicy = new RandomPolicy();
        } else if (type == LoadBalancePolicy.Type.CONSIST_HASH) {
            loadBalancePolicy = new ConsistHashPolicy();
        }

    }

    public static LoadBalanceManager getInstance(LoadBalancePolicy.Type type) {
        if (INSTANCE == null) {
            synchronized (LoadBalanceManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new LoadBalanceManager(type);
                }
            }
        }
        return INSTANCE;
    }

    public <T> T select(List<T> list) {
        return loadBalancePolicy.select(list);
    }
}
