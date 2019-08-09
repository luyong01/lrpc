package com.ranze.literpc.server;

import com.google.common.util.concurrent.RateLimiter;
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
    private int serviceId = 0;
    private Map<Integer, ServiceInfo> serviceIdInfoMap;

    private ServiceManager() {
        serviceMap = new HashMap<>();
        serviceIdInfoMap = new HashMap<>();
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

    public void initServiceMap(String servicePackage) {
        serviceMap.clear();
        Set<Class<?>> classesWithAnnotation = ClassUtil.getClassesWithAnnotation(
                servicePackage, Service.class);
        for (Class<?> clz : classesWithAnnotation) {
            try {
                Object obj = clz.newInstance();
                Class<?> objClass = obj.getClass();
                Class<?>[] interfaces = objClass.getInterfaces();
                if (interfaces.length != 1) {
                    log.error("Service must implements one interface only");
                    throw new RuntimeException("Service must implements one interface only");
                }
                Method[] declaredMethods = objClass.getDeclaredMethods();
                String serviceName = interfaces[0].getCanonicalName();
                for (Method declaredMethod : declaredMethods) {
                    String methodName = declaredMethod.getName();
                    com.ranze.literpc.server.RateLimiter rateLimiterAnnotation =
                            declaredMethod.getAnnotation(com.ranze.literpc.server.RateLimiter.class);

                    RateLimiter rateLimiter = null;
                    if (rateLimiterAnnotation != null) {
                        int limitNum = rateLimiterAnnotation.LimitNum();
                        rateLimiter = RateLimiter.create(limitNum);
                    }

                    int serviceInfoId = serviceId++;
                    String key = generateKey(serviceName, methodName);
                    ServiceInfo serviceInfo = new ServiceInfo(serviceName, declaredMethod, obj,
                            declaredMethod.getParameterTypes()[0], rateLimiter, serviceInfoId);


                    serviceMap.put(key, serviceInfo);
                    serviceIdInfoMap.put(serviceInfoId, serviceInfo);
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

    public ServiceInfo getService(int serviceInfoId) {
        return serviceIdInfoMap.get(serviceInfoId);
    }

    private String generateKey(String ServiceName, String methodName) {
        return ServiceName + "#" + methodName;
    }
}
