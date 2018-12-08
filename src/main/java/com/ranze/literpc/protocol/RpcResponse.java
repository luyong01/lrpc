package com.ranze.literpc.protocol;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RpcResponse {
    private long transactionId;
    private Object result;
    private Throwable exception;

}
