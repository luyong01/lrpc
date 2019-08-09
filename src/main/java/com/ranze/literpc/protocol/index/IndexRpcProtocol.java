package com.ranze.literpc.protocol.index;

import com.google.protobuf.Message;
import com.ranze.literpc.client.LiteRpcClient;
import com.ranze.literpc.client.RpcFuture;
import com.ranze.literpc.compress.Compress;
import com.ranze.literpc.compress.CompressManager;
import com.ranze.literpc.exception.RpcException;
import com.ranze.literpc.protocol.Protocol;
import com.ranze.literpc.protocol.ProtocolType;
import com.ranze.literpc.protocol.RpcRequest;
import com.ranze.literpc.protocol.RpcResponse;
import com.ranze.literpc.server.ServiceInfo;
import com.ranze.literpc.server.ServiceManager;
import com.ranze.literpc.util.StringUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * INDEX_PROTOCOL
 * |---------------------------------------------------------------------------------------|
 * | ["IRPC"][call_id][compress_type][request_index | response_code][body_size][body_data] |
 * |---------------------------------------------------------------------------------------|
 * Magic word and request_index and response_code are 4 bytes
 * call_id is 8 bytes
 * compress_type(for body_data) is 1 byte
 * if response_code is not 0, then body_data means exception message(type of string)
 */
@Slf4j
@ProtocolType(Protocol.Type.INDEX_RPC)
public class IndexRpcProtocol implements Protocol {

    private static final byte[] MAGIC_HEAD = "IRPC".getBytes();
    // from start to body_size
    private static final int HEADER_LEN = 21;

    // TODO: 2019/8/9 暂定
    private static final int MAX_FRAME_LENGTH = 1024 * 1024 * 10;

    private ThreadLocal<Boolean> discardTooLongFrame = ThreadLocal.withInitial(() -> false);
    private ThreadLocal<Long> bytesToDiscard = ThreadLocal.withInitial(() -> 0L);


    public ByteBuf encodeRequest(RpcRequest rpcRequest) throws IOException {
        IndexRpcPacket indexRpcPacket = new IndexRpcPacket();

        indexRpcPacket.setCallId(rpcRequest.getCallId());
        // Compress type 不会太多, byte 够用了, 也许 Compress.Type 中的类型应该用 byte 标记
        indexRpcPacket.setCompressType((byte) rpcRequest.getCompressType().getTypeNo());
        ServiceInfo serviceInfo = ServiceManager.getInstance().getService(rpcRequest.getService().getCanonicalName(),
                rpcRequest.getMethod().getName());
        indexRpcPacket.setRequest_index(serviceInfo.getId());
        Message args = rpcRequest.getArgs();

        Compress compress = CompressManager.getInstance().get(rpcRequest.getCompressType());
        ByteBuf compressedBody = compress.compress(args);
        indexRpcPacket.setBody(compressedBody);

        return encode(indexRpcPacket);
    }

    public RpcRequest decodeRequest(ByteBuf byteBuf) throws IllegalAccessException, IOException, InvocationTargetException {
        IndexRpcPacket indexRpcPacket = decode(byteBuf, true);
        if (indexRpcPacket == null) {
            return null;
        }

        RpcRequest rpcRequest = new RpcRequest();

        ServiceInfo serviceInfo = ServiceManager.getInstance().getService(indexRpcPacket.getRequest_index());
        rpcRequest.setService(serviceInfo.getTarget().getClass().getInterfaces()[0]);
        Compress.Type compressType = CompressManager.getInstance().convert(indexRpcPacket.getCompressType());
        rpcRequest.setCompressType(compressType);
        rpcRequest.setMethod(serviceInfo.getMethod());
        rpcRequest.setCallId(indexRpcPacket.getCallId());

        ByteBuf body = indexRpcPacket.getBody();
        Compress compress = CompressManager.getInstance().get(compressType);
        Message message = compress.unCompress(body, serviceInfo.getRequestClass());

        rpcRequest.setArgs(message);

        return rpcRequest;

    }

    public ByteBuf encodeResponse(RpcResponse rpcResponse) throws IOException {
        IndexRpcPacket rpcPacket = new IndexRpcPacket();
        rpcPacket.setCallId(rpcResponse.getCallId());
        // 转型的原因同上
        rpcPacket.setCompressType((byte) rpcResponse.getCompressType().getTypeNo());
        if (rpcResponse.getException() != null) {
            rpcPacket.setResponse_code(rpcResponse.getException().getCode());

            String message = rpcResponse.getException().getMessage();
            rpcPacket.setExceptionMessage(message);
        } else {
            rpcPacket.setResponse_code(0);

            Message result = rpcResponse.getResult();
            Compress compress = CompressManager.getInstance().get(rpcResponse.getCompressType());
            ByteBuf compressedResult = compress.compress(result);

            rpcPacket.setBody(compressedResult);
        }

        return encode(rpcPacket);

    }

    @Override
    public RpcResponse decodeResponse(ByteBuf byteBuf, LiteRpcClient rpcClient) throws Exception {
        IndexRpcPacket rpcPacket = decode(byteBuf, false);
        if (rpcPacket == null) {
            return null;
        }

        long callId = rpcPacket.getCallId();
        RpcFuture future = rpcClient.getRpcFuture(callId);
        if (future == null) {
            log.warn("Request(callId = {}) may time out, future has been removed", callId);
            return null;
        }

        RpcResponse response = new RpcResponse();
        response.setCallId(callId);
        response.setCompressType(CompressManager.getInstance().convert(rpcPacket.getCompressType()));

        if (StringUtil.isNotEmpty(rpcPacket.getExceptionMessage())) {
            response.setException(new RpcException(rpcPacket.getResponse_code(), rpcPacket.getExceptionMessage()));
        } else {
            Compress compress = CompressManager.getInstance().get(rpcPacket.getCompressType());
            Message message = compress.unCompress(rpcPacket.getBody(), (Class) future.getResponseType());
            response.setResult(message);
        }

        return response;

    }


    private ByteBuf encode(IndexRpcPacket rpcPacket) {
        ByteBuf headerBuf = Unpooled.buffer(HEADER_LEN);
        headerBuf.writeBytes(MAGIC_HEAD);
        headerBuf.writeLong(rpcPacket.getCallId());
        headerBuf.writeByte(rpcPacket.getCompressType());
        if (rpcPacket.getRequest_index() != -1) {
            headerBuf.writeInt(rpcPacket.getRequest_index());
        } else if (rpcPacket.getResponse_code() != -1) {
            headerBuf.writeInt(rpcPacket.getResponse_code());
        } else {
            throw new RuntimeException("IndexRpcPacket must set request index or response code, packet = " + rpcPacket);
        }
        if (rpcPacket.getBody() != null) {
            ByteBuf responseBody = rpcPacket.getBody();
            headerBuf.writeInt(responseBody.readableBytes());
            return Unpooled.wrappedBuffer(headerBuf, responseBody);
        } else {
            String exceptionMessage = rpcPacket.getExceptionMessage();
            ByteBuf exceptionBody = Unpooled.wrappedBuffer(exceptionMessage.getBytes(Charset.forName("UTF-8")));
            headerBuf.writeInt(exceptionBody.readableBytes());
            return Unpooled.wrappedBuffer(headerBuf, exceptionBody);
        }
    }

    private IndexRpcPacket decode(ByteBuf in, boolean decodeRequest) {
        boolean discardingTooLongFrame = discardTooLongFrame.get();
        if (discardingTooLongFrame) {
            discardingTooLongFrame(in);
        }

        if (in.readableBytes() < HEADER_LEN) {
            return null;
        }

        int savedIndex = in.readerIndex();
        byte[] magicBytes = new byte[4];
        in.getBytes(savedIndex, magicBytes);
        if (!Arrays.equals(MAGIC_HEAD, magicBytes)) {
            log.warn("Magic{} is wrong", new String(magicBytes));
            return null;
        }

        in.skipBytes(4);

        IndexRpcPacket indexRpcPacket = new IndexRpcPacket();
        indexRpcPacket.setCallId(in.readLong());
        indexRpcPacket.setCompressType(in.readByte());
        int responseCodeOrRequestIndex = in.readInt();
        if (decodeRequest) {
            indexRpcPacket.setRequest_index(responseCodeOrRequestIndex);
        } else {
            indexRpcPacket.setResponse_code(responseCodeOrRequestIndex);
        }

        int bodySize = in.readInt();
        if (bodySize > MAX_FRAME_LENGTH) {
            exceededFrameLength(in, bodySize);
            return null;
        }

        if (in.readableBytes() < bodySize) {
            in.readerIndex(savedIndex);
            return null;
        }

        ByteBuf body = in.readBytes(bodySize);
        if (decodeRequest) {
            indexRpcPacket.setBody(body);
        } else {
            if (indexRpcPacket.getResponse_code() == 0) {
                indexRpcPacket.setBody(body);
            } else {
                String exceptionMessage = body.toString(Charset.forName("UTF-8"));
                indexRpcPacket.setExceptionMessage(exceptionMessage);
            }
        }

        return indexRpcPacket;
    }

    private void exceededFrameLength(ByteBuf in, int bodyLength) {
        long discard = bodyLength - in.readableBytes();
        if (discard < 0) {
            in.skipBytes(bodyLength);
        } else {
            discardTooLongFrame.set(true);
            bytesToDiscard.set(discard);
            in.skipBytes(in.readableBytes());
        }

        if (discard == 0) {
            discardTooLongFrame.set(false);
        }
    }

    private void discardingTooLongFrame(ByteBuf in) {
        long bytesToDiscard = this.bytesToDiscard.get();
        int localBytesToDiscard = (int) Math.min(bytesToDiscard, in.readableBytes());
        in.skipBytes(localBytesToDiscard);
        bytesToDiscard -= localBytesToDiscard;
        this.bytesToDiscard.set(bytesToDiscard);

        if (bytesToDiscard == 0) {
            discardTooLongFrame.set(false);
        }

    }

}
