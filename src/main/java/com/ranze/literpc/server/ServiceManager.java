package com.ranze.literpc.server;

import com.ranze.literpc.protocol.ProtocolType;
import com.ranze.literpc.util.ClassUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
public class ServiceManager {
    private static ServiceManager INSTANCE;

    private Map<String, ServiceInfo> serviceMap;

    private ServiceManager() {
        serviceMap = new HashMap<>();
    }

    public static ServiceManager getInstance() {
        if (INSTANCE == null) {
            synchronized (ServiceManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ServiceManager();
                }
            }
        }
        return INSTANCE;
    }

    public void initServiceMap() {
        Set<Class<?>> classesWithAnnotation = ClassUtil.getClassesWithAnnotation(
                "com.ranze.literpc.example", Service.class);
        for (Class<?> clz : classesWithAnnotation) {
            try {
                Object obj = clz.newInstance();
                Class<?>[] interfaces = obj.getClass().getInterfaces();
                if (interfaces.length != 1) {
                    log.error("Service must implements one interface only");
                    throw new RuntimeException("Service must implements one interface only");
                }
                Method[] declaredMethods = interfaces[0].getDeclaredMethods();
                String serviceName = interfaces[0].getCanonicalName();
                for (Method declaredMethod : declaredMethods) {
                    String methodName = declaredMethod.getName();
                    String key = generateKey(serviceName, methodName);
                    ServiceInfo serviceInfo = new ServiceInfo(serviceName, declaredMethod, obj,
                            declaredMethod.getParameterTypes()[0]);

                    serviceMap.put(key, serviceInfo);
                }
            } catch (IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            }
        }
        log.info("ServiceMap={}", serviceMap);
    }

    public ServiceInfo getService(String serviceName, String methodName) {
        return serviceMap.get(generateKey(serviceName, methodName));
    }

    private String generateKey(String ServiceName, String methodName) {
        return ServiceName + "#" + methodName;
    }
}
