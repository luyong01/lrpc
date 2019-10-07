package com.ranze.literpc.compress;

import com.google.protobuf.Message;
import com.ranze.literpc.codec.ProtoSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;
import org.xerial.snappy.Snappy;
import org.xerial.snappy.SnappyInputStream;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class SnappyCompress implements Compress {
    @Override
    public ByteBuf compress(Message message) throws IOException {
        byte[] byteArray = message.toByteArray();
        int maxCompressedLength = Snappy.maxCompressedLength(byteArray.length);
        byte[] compressBytes = new byte[maxCompressedLength];
        int compressedLen = Snappy.compress(byteArray, 0, byteArray.length, compressBytes, 0);
        return Unpooled.wrappedBuffer(compressBytes, 0, compressedLen);
    }

    @Override
    public Message unCompress(ByteBuf byteBuf, Class clz) {
        InputStream inputStream = new ByteBufInputStream(byteBuf);
        try (SnappyInputStream snappyInputStream = new SnappyInputStream(inputStream)) {
            return ProtoSerializer.deserialize(clz, snappyInputStream);
        } catch (Exception e) {
            log.warn("Snappy uncompress failed, exception = ", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
