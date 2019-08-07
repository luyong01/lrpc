package com.ranze.literpc.compress;

import com.google.protobuf.Message;
import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public interface Compress {
    enum Type {
        NONE(1, "none"),
        GZIP(2, "gzip"),
        SNAPPY(3, "snappy");

        private int typeNo;
        private String typeName;

        Type(int typeNo, String typeName) {
            this.typeNo = typeNo;
            this.typeName = typeName;
        }

        public int getTypeNo() {
            return typeNo;
        }

        public String getTypeName() {
            return typeName;
        }
    }

    ByteBuf compress(Message message) throws IOException;

    Message unCompress(ByteBuf byteBuf, Class clz) throws IllegalAccessException, IOException, InvocationTargetException;
}
