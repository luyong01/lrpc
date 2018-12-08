package com.ranze.literpc.client;

import com.ranze.literpc.protocol.Protocol;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RpcClientOption {
    private Protocol.Type protocolType = Protocol.Type.LITE_PROTOCOL;
}
