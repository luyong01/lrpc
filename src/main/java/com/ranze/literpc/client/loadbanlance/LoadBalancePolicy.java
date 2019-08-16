package com.ranze.literpc.client.loadbanlance;

import java.util.List;

public interface LoadBalancePolicy {
    enum Type {
        ROUND_ROBIN(1, "round robin"),
        RANDOM(2, "random"),
        CONSIST_HASH(3, "consist hash");

        private int typeNo;
        private String typeName;

        Type(int typeNo, String typeName) {
            this.typeNo = typeNo;
            this.typeName = typeName;
        }

        int getTypeNo() {
            return typeNo;
        }

        String getTypeName() {
            return typeName;
        }
    }

    <T> T select(List<T> list);

}
