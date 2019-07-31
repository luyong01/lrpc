package com.ranze.literpc.protocol.lite;

import com.google.protobuf.Message;
import com.ranze.literpc.client.LiteRpcClient;
import com.ranze.literpc.client.RpcFuture;
import com.ranze.literpc.exception.RpcException;
import com.ranze.literpc.protocol.Protocol;
import com.ranze.literpc.protocol.ProtocolType;
import com.ranze.literpc.protocol.RpcRequest;
import com.ranze.literpc.protocol.RpcResponse;
import com.ranze.literpc.server.ServiceInfo;
import com.ranze.literpc.server.ServiceManager;
import com.ranze.literpc.codec.ProtoSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Arrays;

/**
 * LITE_PROTOCOL
 * |------------------------------------------------------|
 * | ["LRPC"][body_size][meta_size][meta_data][body_data] |
 * |______________________________________________________|
 * body_size = meta_size + real_body_size
 * meta_data:[call_id][compress_type][request_meta|response_meta]
 * request_meta:[service_name][method_name]
 * response_meta:[code][reason]
 */
@ProtocolType(Protocol.Type.LITE_PROTOCOL)
@Slf4j
public class LiteProtocol implements Protocol {
    private static LiteProtocol INSTANCE;
    private static final byte[] MAGIC_HEAD = "LRPC".getBytes();
    private static final int HEADER_LEN = 12;

    public static LiteProtocol getInstance() {
        if (INSTANCE == null) {
            synchronized (LiteProtocol.class) {
                if (INSTANCE == null) {
                    INSTANCE = new LiteProtocol();
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public ByteBuf encodeRequest(RpcRequest rpcRequest) throws Exception {
        LiteRpcPacket liteRpcPacket = new LiteRpcPacket();

        LiteRpcProto.RpcMeta.Builder rpcMetaBuilder = LiteRpcProto.RpcMeta.newBuilder();
        rpcMetaBuilder.setCallId(rpcRequest.getCallId());
        //todo 这里暂定compress为0，表示未压缩
        rpcMetaBuilder.setCompressType(0);
        LiteRpcProto.RpcRequest.Builder requestBuilder = LiteRpcProto.RpcRequest.newBuilder();
        requestBuilder.setServiceName(rpcRequest.getService().getCanonicalName());
        requestBuilder.setMethodName(rpcRequest.getMethod().getName());
        rpcMetaBuilder.setRequest(requestBuilder.build());

        liteRpcPacket.setRpcMeta(rpcMetaBuilder.build());

        //todo 这部分后续要添加压缩 其它编、解码方法也需要
        Message arg = rpcRequest.getArgs();
        byte[] argBytes = ProtoSerializer.serialize(arg);
        liteRpcPacket.setBody(Unpooled.wrappedBuffer(argBytes));

        return encode(liteRpcPacket);
    }

    @Override
    public RpcRequest decodeRequest(ByteBuf byteBuf) throws Exception {
        LiteRpcPacket liteRpcPacket = decode(byteBuf);
        RpcRequest request = new RpcRequest();
        if (liteRpcPacket == null) {
            return null;
        }

        LiteRpcProto.RpcMeta rpcMeta = liteRpcPacket.getRpcMeta();
        request.setCallId(rpcMeta.getCallId());

        LiteRpcProto.RpcRequest requestMeta = rpcMeta.getRequest();
        ServiceInfo serviceInfo = ServiceManager.getInstance().getService(
                requestMeta.getServiceName(), requestMeta.getMethodName());
        request.setService(serviceInfo.getTarget().getClass().getInterfaces()[0]);
        request.setMethod(serviceInfo.getMethod());

        ByteBuf bodyBuf = liteRpcPacket.getBody();
        Message body = ProtoSerializer.deserialize(serviceInfo.getRequestClass(),
                new ByteBufInputStream(bodyBuf));
        request.setArgs(body);

        return request;
    }

    @Override
    public ByteBuf encodeResponse(RpcResponse rpcResponse) throws Exception {
        LiteRpcPacket liteRpcPacket = new LiteRpcPacket();

        LiteRpcProto.RpcMeta.Builder rpcMetaBuilder = LiteRpcProto.RpcMeta.newBuilder();
        rpcMetaBuilder.setCallId(rpcResponse.getCallId());
        rpcMetaBuilder.setCompressType(0);
        LiteRpcProto.RpcResponse.Builder responseBuilder = LiteRpcProto.RpcResponse.newBuilder();
        if (rpcResponse.getException() != null) {
            responseBuilder.setCode(rpcResponse.getException().getCode());
            responseBuilder.setReason(rpcResponse.getException().getReason());
        } else {
            responseBuilder.setCode(0);
            responseBuilder.setReason("ok");
            byte[] responseBytes = ProtoSerializer.serialize(rpcResponse.getResult());
            liteRpcPacket.setBody(Unpooled.wrappedBuffer(responseBytes));
        }

        rpcMetaBuilder.setResponse(responseBuilder.build());
        liteRpcPacket.setRpcMeta(rpcMetaBuilder.build());

        return encode(liteRpcPacket);
    }

    @Override
    public RpcResponse decodeResponse(ByteBuf byteBuf, LiteRpcClient rpcClient) throws Exception {
        LiteRpcPacket liteRpcPacket = decode(byteBuf);
        if (liteRpcPacket == null) {
            return null;
        }

        LiteRpcProto.RpcMeta rpcMeta = liteRpcPacket.getRpcMeta();
        long callId = rpcMeta.getCallId();
        RpcFuture future = rpcClient.getRpcFuture(callId);
        if (future == null) {
            log.warn("Request(callId = {}) time out, future has been removed", callId);
            return null;
        }

        RpcResponse response = new RpcResponse();

        response.setCallId(callId);

        if (rpcMeta.getResponse().getCode() == 0) {
            ByteBuf bodyBuf = liteRpcPacket.getBody();
            Message body = ProtoSerializer.deserialize((Class) future.getResponseType(),
                    new ByteBufInputStream(bodyBuf));
            response.setResult(body);
        } else {
            LiteRpcProto.RpcResponse rpcResponse = rpcMeta.getResponse();
            response.setException(new RpcException(rpcResponse.getCode(), rpcResponse.getReason()));
        }
        return response;
    }

    private ByteBuf encode(LiteRpcPacket liteRpcPacket) throws IOException {
        ByteBuf headerBuf = Unpooled.buffer(HEADER_LEN);
        headerBuf.writeBytes(MAGIC_HEAD);

        LiteRpcProto.RpcMeta rpcMeta = liteRpcPacket.getRpcMeta();
        int metaSize = rpcMeta.getSerializedSize();

        int bodySize = metaSize;
        if (liteRpcPacket.getBody() != null) {
            bodySize = metaSize + liteRpcPacket.getBody().readableBytes();
        }

        headerBuf.writeInt(bodySize);
        headerBuf.writeInt(metaSize);

        ByteBuf rpcMetaBuf = Unpooled.buffer(metaSize);
        liteRpcPacket.getRpcMeta().writeTo(new ByteBufOutputStream(rpcMetaBuf));

        return Unpooled.wrappedBuffer(headerBuf, rpcMetaBuf, liteRpcPacket.getBody());
    }

    private LiteRpcPacket decode(ByteBuf in) {
        if (in.readableBytes() < HEADER_LEN) {
            return null;
        }

        // mark for reset later if error occurred
        in.markReaderIndex();

        byte[] magicBytes = new byte[4];
        in.readBytes(magicBytes);
        if (!Arrays.equals(magicBytes, MAGIC_HEAD)) {
            log.warn("Magic{} is wrong", new String(magicBytes));
            in.resetReaderIndex();
            return null;
        }

        int bodySize = in.readInt();
        if (in.readableBytes() < bodySize) {
            in.resetReaderIndex();
            return null;
        }

        try {
            LiteRpcPacket liteRpcPacket = new LiteRpcPacket();

            int metaSize = in.readInt();
            byte[] metaBytes = new byte[metaSize];
            in.readBytes(metaBytes);
            LiteRpcProto.RpcMeta rpcMeta = (LiteRpcProto.RpcMeta) ProtoSerializer.deserialize(
                    LiteRpcProto.RpcMeta.class, metaBytes);
            liteRpcPacket.setRpcMeta(rpcMeta);

            int realBodySize = bodySize - metaSize;
            if (realBodySize != 0) {
                ByteBuf bodyBuf = in.readBytes(realBodySize);
                liteRpcPacket.setBody(bodyBuf);
            }

            return liteRpcPacket;
        } catch (Exception e) {
            log.debug("decode failed, exception = {}", e.getMessage());
            in.resetReaderIndex();
            throw new RuntimeException(e);
        }

    }
}
