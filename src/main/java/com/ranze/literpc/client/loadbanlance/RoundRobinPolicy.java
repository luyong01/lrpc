package com.ranze.literpc.client.loadbanlance;

import java.util.ArrayList;
import java.util.List;

public class RoundRobinPolicy implements LoadBalancePolicy {
    private int pos = 0;

    @Override
    public <T> T select(List<T> list) {
        // 拷贝 list, 防止因为服务地址的更新导致在遍历中发生变化
        List<T> curList = new ArrayList<>(list);
        T selected;
        synchronized (this) {
            if (pos >= list.size()) {
                pos = 0;
            }
            selected = curList.get(pos);
            pos++;
        }
        return selected;
    }
}
