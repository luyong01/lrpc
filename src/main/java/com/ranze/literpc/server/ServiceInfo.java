package com.ranze.literpc.server;

import com.google.common.util.concurrent.RateLimiter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.lang.reflect.Method;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class ServiceInfo {
    private String serviceName;
    private Method method;
    private Object target;
    private Class requestClass;
    private RateLimiter rateLimiter;
}
