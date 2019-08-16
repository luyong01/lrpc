package com.ranze.literpc.client.loadbanlance;

import java.util.ArrayList;
import java.util.List;

public class RandomPolicy implements LoadBalancePolicy {
    @Override
    public <T> T select(List<T> list) {
        List<T> curList = new ArrayList<>(list);
        java.util.Random random = new java.util.Random();
        int randomPos = random.nextInt(curList.size());
        return curList.get(randomPos);

    }
}
