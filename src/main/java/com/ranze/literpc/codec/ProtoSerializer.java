package com.ranze.literpc.codec;


import com.google.protobuf.Message;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ProtoSerializer {
    private static Map<Class<?>, Method> parseFromMethodCache;
    private static Map<Class<?>, Method> defaultInstanceMethodCache;

    static {
        parseFromMethodCache = new HashMap<>();
        defaultInstanceMethodCache = new HashMap<>();
    }

    public static byte[] serialize(Message obj) {
        return obj.toByteArray();
    }

    public static Message deserialize(Class clz, byte[] bytes) throws InvocationTargetException, IllegalAccessException {
        Method parseFromMethod = getParseFromMethod(clz);
        return (Message) parseFromMethod.invoke(null, bytes);
    }

    public static Message deserialize(Class clz, InputStream inputStream) throws InvocationTargetException,
            IllegalAccessException, IOException {
        Method parseFromMethod = getDefaultInstanceMethod(clz);
        Message proto = (Message) parseFromMethod.invoke(null);
        return proto.newBuilderForType().mergeFrom(inputStream).build();
    }

    private static Method getParseFromMethod(Class<?> clz) {
        Method method = parseFromMethodCache.get(clz);
        if (method == null) {
            try {
                method = clz.getDeclaredMethod("parseFrom", byte[].class);
                parseFromMethodCache.put(clz, method);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        return method;
    }

    private static Method getDefaultInstanceMethod(Class<?> clz) {
        Method method = defaultInstanceMethodCache.get(clz);
        if (method == null) {
            try {
                method = clz.getDeclaredMethod("getDefaultInstance");
                defaultInstanceMethodCache.put(clz, method);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        return method;
    }

}
