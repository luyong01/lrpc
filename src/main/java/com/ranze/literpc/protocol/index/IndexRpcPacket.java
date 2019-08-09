package com.ranze.literpc.protocol.index;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class IndexRpcPacket {
    private long callId;
    private byte compressType;
    private int request_index = -1;
    private int response_code = -1;
    private ByteBuf body;
    private String exceptionMessage;
}
