package com.ranze.literpc.protocol;

import com.google.protobuf.Message;
import com.ranze.literpc.compress.Compress;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.lang.reflect.Method;

@Getter
@Setter
@ToString
public class RpcRequest {
    private long callId;
    private Class service;
    private Method method;
    // method的参数有且只有一个，并且是protobuf形式的
    private Message args;
    private Compress.Type compressType;

}
