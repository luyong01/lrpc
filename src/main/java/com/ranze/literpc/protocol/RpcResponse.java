package com.ranze.literpc.protocol;

import com.google.protobuf.Message;
import com.ranze.literpc.compress.Compress;
import com.ranze.literpc.exception.RpcException;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RpcResponse {
    private long callId;
    private Message result;
    private RpcException exception;
    private Compress.Type compressType;

}
