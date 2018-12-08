package com.ranze.literpc.protocol;

import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Method;

@Getter
@Setter
public class RpcRequest {
    private long transactionId;
    private Object target;
    private Method method;
    private Object[] args;

}
