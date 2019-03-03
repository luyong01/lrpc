package com.ranze.literpc.protocol.lite;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LiteRpcPacket {
    private LiteRpcProto.RpcMeta rpcMeta;
    private ByteBuf body;
}
