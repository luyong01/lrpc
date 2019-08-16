package com.ranze.literpc.client.loadbanlance;

import com.google.common.hash.Hashing;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ConsistHashPolicy implements LoadBalancePolicy {
    private String consumerIp;

    public ConsistHashPolicy() {
        consumerIp = getLocalIp();
    }

    @Override
    public <T> T select(List<T> list) {
        List<T> curList = new ArrayList<>(list);
        int index = Hashing.consistentHash(consumerIp.hashCode(), curList.size());
        return curList.get(index);
    }

    private String getLocalIp() {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            return addr.getHostAddress();
        } catch (UnknownHostException e) {
            log.warn("Get local ip caused exception: {}", e.getMessage());
            throw new RuntimeException(e);
        }

    }
}
