package com.ranze.literpc.protocol;

import com.google.protobuf.Message;
import com.ranze.literpc.exception.RpcException;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RpcResponse {
    private long callId;
    private Message result;
    private RpcException exception;

}
