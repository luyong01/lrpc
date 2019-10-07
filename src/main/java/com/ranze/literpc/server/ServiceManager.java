package com.ranze.literpc.server;

import com.google.common.util.concurrent.RateLimiter;
import com.ranze.literpc.util.ClassUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@Slf4j
public class ServiceManager {
    private static volatile ServiceManager INSTANCE;

    private Map<String, ServiceInfo> serviceImplMap;
    private int serviceId = 0;
    private Map<Integer, String> serviceIdKeyMap;
    private Map<String, Integer> serviceKeyIdMap;

    private ServiceManager() {
        serviceImplMap = new HashMap<>();
        serviceIdKeyMap = new HashMap<>();
        serviceKeyIdMap = new HashMap<>();
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

    public void initServiceIdKeyMap(String servicePackage) {
        serviceIdKeyMap.clear();
        serviceKeyIdMap.clear();

        Set<Class<?>> classes = ClassUtil.getClasses(servicePackage, true);
        // 排序保证每次调用都一致
        TreeSet<String> keySet = new TreeSet<>();
        for (Class<?> clz : classes) {
            if (!clz.isInterface()) {
                continue;
            }
            String className = clz.getCanonicalName();
            Method[] declaredMethods = clz.getDeclaredMethods();
            for (Method method : declaredMethods) {
                String methodName = method.getName();
                String serviceKey = generateKey(className, methodName);
                keySet.add(serviceKey);
            }
        }
        for (String serviceKey : keySet) {
            int id = serviceId++;
            serviceIdKeyMap.put(id, serviceKey);
            serviceKeyIdMap.put(serviceKey, id);
        }

        log.info("ServiceIdKeyMap = {}", serviceIdKeyMap);

    }

    public void initServiceMap(String serviceImplPackage) {
        serviceImplMap.clear();
        Set<Class<?>> classesWithAnnotation = ClassUtil.getClassesWithAnnotation(
                serviceImplPackage, Service.class);
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

                    String key = generateKey(serviceName, methodName);
                    ServiceInfo serviceInfo = new ServiceInfo(serviceName, declaredMethod, obj,
                            declaredMethod.getParameterTypes()[0], rateLimiter);


                    serviceImplMap.put(key, serviceInfo);
                }
            } catch (IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            }
        }
        log.info("ServiceMap={}", serviceImplMap);
    }

    public ServiceInfo getService(String serviceName, String methodName) {
        return serviceImplMap.get(generateKey(serviceName, methodName));
    }

    public ServiceInfo getService(int serviceInfoId) {
        return serviceImplMap.get(serviceIdKeyMap.get(serviceInfoId));
    }

    public int getServiceId(String serviceName, String methodName) {
        return serviceKeyIdMap.get(generateKey(serviceName, methodName));
    }

    private String generateKey(String ServiceName, String methodName) {
        return ServiceName + "#" + methodName;
    }
}
