package com.ranze.literpc.util;

import com.ranze.literpc.protocol.Protocol;
import com.ranze.literpc.protocol.ProtocolType;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;

@Slf4j
public class ProtocolUtil {
    public static void initProtocolMap(Map<Protocol.Type, Protocol> protocolMap) {
        if (protocolMap == null) {
            return;
        }
        Set<Class<?>> classesWithAnnotation = ClassUtil.getClassesWithAnnotation(
                "com.ranze.literpc.protocol", ProtocolType.class);
        for (Class<?> clz : classesWithAnnotation) {
            try {
                Object obj = clz.newInstance();
                if (obj instanceof Protocol) {
                    Protocol protocol = (Protocol) obj;
                    Protocol.Type type = protocol.getClass().getAnnotation(ProtocolType.class).value();
                    protocolMap.put(type, protocol);
                } else {
                    log.warn("Bean annotated with 'ProtocolType' must be sub type of interface 'Protocol'");
                }
            } catch (InstantiationException | IllegalAccessException e) {
                log.warn("Create new instance failed, msg = {}", e.getMessage());
            }
        }
    }
}
