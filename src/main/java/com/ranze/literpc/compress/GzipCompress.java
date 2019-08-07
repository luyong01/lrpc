package com.ranze.literpc.compress;

import com.google.protobuf.Message;
import com.ranze.literpc.codec.ProtoSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@Slf4j
public class GzipCompress implements Compress {

    @Override
    public ByteBuf compress(Message message) throws IOException {
        int serializedSize = message.getSerializedSize();
        ByteBuf wrappedBuffer = Unpooled.buffer(serializedSize);
        OutputStream outputStream = new ByteBufOutputStream(wrappedBuffer);
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream);
        message.writeTo(gzipOutputStream);
        gzipOutputStream.close();
        return wrappedBuffer;

    }

    @Override
    public Message unCompress(ByteBuf byteBuf, Class clz) {
        InputStream inputStream = new ByteBufInputStream(byteBuf);
        try (GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream)) {
            return ProtoSerializer.deserialize(clz, gzipInputStream);
        } catch (Exception e) {
            log.warn("gzip uncompress failed, exception = {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
